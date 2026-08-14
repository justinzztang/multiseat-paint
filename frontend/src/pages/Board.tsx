import React, {useCallback, useEffect, useRef, useState} from "react";

import {Client, type IMessage} from "@stomp/stompjs";

import type {Point, WebSocketMessage, Color} from "../utils/Utils.ts";

import {hexToColor} from "../utils/Utils.ts";

import {bresenhamLine} from "../utils/Bresenham.ts";

function Board(){

    const GRID_SIZE = 1;
    var width = 1000;
    var height = 1000;

    // the top canvas
    const canvasRef = useRef<HTMLCanvasElement | null>(null);
    // the top canvas context
    const ctxRef = useRef<CanvasRenderingContext2D | null>(null);
    //the background canvas
    const backgroundCanvasRef = useRef <HTMLCanvasElement | null>(null);
    //the background canvas context
    const backgroundCtxRef = useRef<CanvasRenderingContext2D | null>(null);

    // the canvas used for making the putimagedata syncs normal
    const syncCanvas = new OffscreenCanvas(1,1);
    // the sync canvas context
    const syncCtx = syncCanvas.getContext("2d");

    // last point drawn, used for connecting lines smoothly
    const lastPointRef = useRef<Point | null>(null);
    // if the pen is down
    const penDownRef = useRef(false);

    const colorRef = useRef<HTMLInputElement | null>(null);

    const dpr = window.devicePixelRatio || 1;

    const snap = (v:number) => Math.round(v * dpr) / dpr;

    const snapToGrid = (v:number) => Math.round(v*GRID_SIZE)/GRID_SIZE;

    useEffect(() => {
        if(!canvasRef.current || !backgroundCanvasRef.current){
            console.log("ERROR: canvas element not detected");
            return;
        }
        canvasRef.current.width = Math.round(width*dpr);
        canvasRef.current.height = Math.round(height*dpr);
        //canvasRef.current.style.width = width+'px';
        //canvasRef.current.style.height = height+'px';
        backgroundCanvasRef.current.width = Math.round(width*dpr);
        backgroundCanvasRef.current.height = Math.round(height*dpr);
        //backgroundCanvasRef.current.style.width = width+'px';
        //backgroundCanvasRef.current.style.height = height+'px';



        ctxRef.current = canvasRef.current.getContext("2d");
        backgroundCtxRef.current = backgroundCanvasRef.current.getContext("2d");
        if(!ctxRef.current || !backgroundCtxRef.current){
            console.log("ERROR: canvas context not detected");
            return;
        }
        ctxRef.current.scale(window.devicePixelRatio, window.devicePixelRatio);
        ctxRef.current.imageSmoothingEnabled = false;
        backgroundCtxRef.current.scale(window.devicePixelRatio, window.devicePixelRatio);
        backgroundCtxRef.current.imageSmoothingEnabled = false;

        if(!colorRef.current){
            console.log("ERROR: color picker not detected");
            return;
        }

        }, []);

    const getPoint = useCallback( //TODO need a lot of modifications when you add zooming and stuff
        (e: React.PointerEvent<HTMLCanvasElement>) => {
            const rect = e.currentTarget.getBoundingClientRect();
            return { x: e.clientX - rect.left, y: e.clientY - rect.top };
        },[]
    );

    function drawCell(cellX:number,cellY:number,ctx:CanvasRenderingContext2D, color:Color){
        const left = snap(cellX);
        const top = snap(cellY);
        const right = snap(cellX + GRID_SIZE);
        const bottom = snap(cellY + GRID_SIZE);

        ctx.fillStyle = `rgba(${color.r}, ${color.g}, ${color.b}, ${color.a})`;
        ctx.fillRect(left, top, right - left, bottom - top);
    }

    const drawLine = useCallback(
        (prevX : number, prevY: number, newX:number, newY:number) => {
            const ctx = ctxRef.current;
            if (!ctx) return;
            const markedCells = bresenhamLine(prevX,prevY,newX,newY);
            if(!colorRef.current) return;
            let penColor = hexToColor(colorRef.current.value);
            if (!penColor) penColor = {r:0,g:0,b:0,a:255};
            for (const cell of markedCells) {
                drawCell(cell.x , cell.y, ctx,penColor);
            }
        },[drawCell, bresenhamLine]
    );

    const transferTopToBackground = useCallback(
        () => {
            const bg = backgroundCtxRef.current;
            const fg = canvasRef.current;
            const fgCtx = ctxRef.current;
            if (!bg || !fg || !fgCtx) return;

            bg.save();
            bg.setTransform(1,0,0,1,0,0);
            bg.drawImage(fg, 0, 0);
            bg.restore();
            fgCtx.clearRect(0,0,fg.width, fg.height);
        },[]
    );

    //websocket stuff
    const stompRef = useRef<Client | null>(null);
    const [status, setStatus] = useState("disconnected");
    const [userID, setUserID] = useState(-1);

    //this one works
    const sendWebSocketMessage = useCallback(
        (msg:WebSocketMessage) => {
            console.log(msg);
            const client = stompRef.current;
            if (client?.connected){
                client.publish({
                    destination: "/app/handleaction",
                    body: JSON.stringify(msg)
                })
            }
        },
        []
    );

    //unknown
    const receiveWebSocketMessage = useCallback(
        (msg:WebSocketMessage) => {
            switch (msg.type) {
                case "BeginStroke":
                    console.log("received beginstroke");
                    break;
                case "IDAssignment":
                    console.log("assigned user id: " + msg.userID);
                    setUserID(msg.userID);
                    break;
                default:
                    console.log("received some json thing");
            }
        },
        []
    );

    const receiveSyncMessage = useCallback(
        (msg:IMessage) => {
            const bytes:Uint8Array = msg.binaryBody;
            const startingX = (bytes[0] << 8) + (bytes[1]);
            const startingY = (bytes[2] << 8) + (bytes[3]);
            const width = (bytes[4] << 8) + (bytes[5]);
            const height = (bytes[6] << 8) + (bytes[7]);
            const bg = backgroundCtxRef.current;
            if (!bg) return;

            const imageData = bg.createImageData(width, height);
            const data = imageData.data;

            let index = 8;
            for(let y = startingY; y < startingY + height; y++){
                for(let x = startingX; x < startingX + width; x++){
                    data[index-8] = bytes[index];
                    data[index-7] = bytes[index+1];
                    data[index-6] = bytes[index+2];
                    data[index-5] = bytes[index+3];
                    index+=4;
                }
            }

            if(syncCanvas.width !== width || syncCanvas.height !== height){
                syncCanvas.width = width;
                syncCanvas.height = height;
            }

            syncCtx?.putImageData(imageData,0,0);

            bg.save();
            //bg.setTransform(1,0,0,1,0,0);
            bg.drawImage(syncCanvas,startingX,startingY);
            bg.restore();
            transferTopToBackground();
        },
        [drawCell, transferTopToBackground]
    );

    //problem
    const connectWebSocket = useCallback(() => {
        setStatus("connecting");

        const client = new Client({
            brokerURL: "ws://localhost:8080/update-websocket",
            reconnectDelay: 5000,
            //debug: (str) => console.log("[stomp]", str),
            onConnect: () => {
                console.log("idk");
                setStatus("connected");
                //its gotta be here
                client.subscribe("/update/whattoupdate", messageCallback);

                client.subscribe("/user/update/whattoupdate", messageCallback);

                function messageCallback(message: IMessage) {
                    try {
                        if (message.headers["content-type"].includes("application/json")) {
                            const msg = JSON.parse(message.body) as WebSocketMessage;
                            receiveWebSocketMessage(msg);
                        } else if (message.isBinaryBody) {
                            receiveSyncMessage(message);
                        } else {
                            console.log("??????????");
                            /*const msg = JSON.parse(message.body) as WebSocketMessage;
                            receiveWebSocketMessage(msg);*/
                        }
                    } catch (err) {
                        console.error("malformed message:", message.body, err);
                    }

                }

            }
        });

        client.activate();
        stompRef.current = client;

        console.log(status);

    }, [receiveWebSocketMessage, receiveSyncMessage]);

    const disconnect = useCallback(() => {
        stompRef.current?.deactivate();
        stompRef.current = null;
    }, []);

    useEffect(() => () => {stompRef.current?.deactivate();}, []);

    const beginStroke = useCallback(
        (e: React.PointerEvent<HTMLCanvasElement>) => {
            const { x, y } = getPoint(e);
            const ctx = ctxRef.current;
            if (!ctx) return;
            console.log("beginStroke");
            console.log(x);

            const cellX = Math.floor(x / GRID_SIZE) * GRID_SIZE;
            const cellY = Math.floor(y / GRID_SIZE) * GRID_SIZE;

            if(!ctxRef.current) return;
            if(!colorRef.current) return;
            let penColor = hexToColor(colorRef.current.value);
            if (!penColor) penColor = {r:0,g:0,b:0,a:255};
            drawCell(cellX, cellY, ctxRef.current, penColor);
            console.log(cellX);

            lastPointRef.current = {x,y};
            penDownRef.current = true;

            //send message
            sendWebSocketMessage({type: "BeginStroke", userID: userID, x: cellX, y: cellY, thickness:1,r:penColor.r,g:penColor.g,b:penColor.b,a:penColor.a});
        },
        [drawCell, getPoint, sendWebSocketMessage, userID]
    );



    const pointerMove = useCallback(
        (e:React.PointerEvent<HTMLCanvasElement>) =>{
            if (!lastPointRef.current || !penDownRef.current) return;
            const {x,y} = getPoint(e);
            const last = lastPointRef.current;
            drawLine(last.x, last.y, x, y);
            lastPointRef.current = {x,y};

            if(!colorRef.current) return;
            let penColor = hexToColor(colorRef.current.value);
            if (!penColor) penColor = {r:0,g:0,b:0,a:255};

            //send message
            sendWebSocketMessage({type: "Draw", userID: userID,
                prevX:snapToGrid(last.x), prevY:snapToGrid(last.y),
                x: snapToGrid(x), y: snapToGrid(y),thickness:1,r:penColor.r,g:penColor.g,b:penColor.b,a:penColor.a});


        },
        [getPoint, drawLine, sendWebSocketMessage, userID]
    );

    const pointerUp = useCallback(
        (e:React.PointerEvent<HTMLCanvasElement>) => {
            if(!penDownRef.current) return;

            penDownRef.current = false;
            lastPointRef.current = null;

            //send message
            const {x,y} = getPoint(e);
            sendWebSocketMessage({type: "EndStroke", userID: userID, x: Math.floor(x / GRID_SIZE) * GRID_SIZE, y: Math.floor(y / GRID_SIZE) * GRID_SIZE, thickness: 1});
        },
        [getPoint, sendWebSocketMessage, userID]
    );

    const sendUndo = useCallback(
        () => {
            sendWebSocketMessage({type: "Undo", userID: userID});
        },[ sendWebSocketMessage, userID]
    );
    const sendRedo = useCallback(
        () => {
            sendWebSocketMessage({type: "Redo", userID: userID});
        },[ sendWebSocketMessage, userID]
    );

    const breakpoint = useCallback(
        () => {
            sendWebSocketMessage({type: "breakpoint", userID: userID});
        },[ sendWebSocketMessage, userID]
    );

    return(
        <>
            <div className="bg-gray-200 h-max">
                <button onClick={connectWebSocket}>connect|</button>
                <button onClick={sendUndo}>undo|</button>
                <button onClick={sendRedo}>redo|</button>
                <button onClick={breakpoint}>breakpoint</button>
                <input ref={colorRef} type="color" id="strokeColor" name="strokeColor"></input>
                <div className="relative ml-10" style={{ width: `${width}px`, height: `${height}px` }}>
                    <canvas
                        ref={backgroundCanvasRef}
                        className="absolute top-0 left-0 bg-white image-render-[pixelated] z-10"
                        style={{ width: `${width}px`, height: `${height}px` }}
                    />
                    <canvas
                        ref={canvasRef}
                        className="absolute top-0 left-0 image-render-[pixelated] z-50"
                        style={{ width: `${width}px`, height: `${height}px` }}
                        onPointerDown={beginStroke}
                        onPointerMove={pointerMove}
                        onPointerUp={pointerUp}
                        onPointerOut={pointerUp}
                    />
                </div>
                <h1>footer</h1>
            </div>
        </>
    )
}

export default Board;