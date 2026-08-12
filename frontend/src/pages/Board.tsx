import React, {useCallback, useEffect, useRef, useState} from "react";

import {Client, type IMessage} from "@stomp/stompjs";

import type {Point, WebSocketMessage, Color} from "../utils/Utils.ts";

import {bresenhamLine} from "../utils/Bresenham.ts";

function Board(){

    //websocket stuff
    const stompRef = useRef<Client | null>(null);
    const [status, setStatus] = useState("disconnected");

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
                default:
                    console.log("received some json thing");
            }
        },
        []
    );

    //works, at least once
    const receiveSyncMessage = useCallback(
        (msg:IMessage) => {
            console.log("received sync message @", performance.now());
            const bytes:Uint8Array = msg.binaryBody;
            const startingX = bytes[0];
            const startingY = bytes[1];
            const width = bytes[2];
            const height = bytes[3];

            console.log(bytes);

            const bg = backgroundCtxRef.current;
            if (!bg) return;
            let index = 4;
            for(let y = startingY; y < startingY + height; y++){
                for(let x = startingX; x < startingX + width; x++){

                    let cellColor:Color = {r:bytes[index],g:bytes[index+1],b:bytes[index+2],a:bytes[index+3]};
                    index+=4;
                    console.log(cellColor);
                    drawCell(x, y, bg, cellColor);

                }
            }
        },
        [drawCell]
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
                client.subscribe("/update/whattoupdate", (message:IMessage) => {
                    console.log("received some message");
                    try{
                        console.log("inside try");
                        if(message.isBinaryBody){
                            console.log("binbod");
                            receiveSyncMessage(message);
                        }
                        else {
                            console.log("else")
                            const msg = JSON.parse(message.body) as WebSocketMessage;
                            receiveWebSocketMessage(msg);
                        }
                    } catch (err){
                        console.error("malformed message:", message.body, err);
                    }

                });

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


    const GRID_SIZE = 1;
    var width = 10;
    var height = 10;

    // the top canvas
    const canvasRef = useRef<HTMLCanvasElement | null>(null);
    // the top canvas context
    const ctxRef = useRef<CanvasRenderingContext2D | null>(null);
    //the background canvas
    const backgroundCanvasRef = useRef <HTMLCanvasElement | null>(null);
    //the background canvas context
    const backgroundCtxRef = useRef<CanvasRenderingContext2D | null>(null);
    // last point drawn, used for connecting lines smoothly
    const lastPointRef = useRef<Point | null>(null);
    // if the pen is down
    const penDownRef = useRef(false);

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
        backgroundCanvasRef.current.width = Math.round(width*dpr);
        backgroundCanvasRef.current.height = Math.round(height*dpr);

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
        console.log(ctx.fillStyle)
        ctx.fillRect(left, top, right - left, bottom - top);
    }

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
            drawCell(cellX, cellY, ctxRef.current, {r:0,g:0,b:0,a:255});
            console.log(cellX);

            lastPointRef.current = {x,y};
            penDownRef.current = true;

            //send message
            sendWebSocketMessage({type: "BeginStroke", userID: 0, x: cellX, y: cellY, thickness:1});

        },
        [drawCell, getPoint]
    );

    const drawLine = useCallback(
        (prevX : number, prevY: number, newX:number, newY:number) => {
            const ctx = ctxRef.current;
            if (!ctx) return;
            const markedCells = bresenhamLine(prevX,prevY,newX,newY);
            for (const cell of markedCells) {
                drawCell(cell.x , cell.y, ctx,{r:0,g:0,b:0,a:255});
            }
        },[drawCell, bresenhamLine]
    );

    const pointerMove = useCallback(
        (e:React.PointerEvent<HTMLCanvasElement>) =>{
            if (!lastPointRef.current || !penDownRef.current) return;
            const {x,y} = getPoint(e);
            const last = lastPointRef.current;
            drawLine(last.x, last.y, x, y);
            lastPointRef.current = {x,y};

            //send message
            sendWebSocketMessage({type: "Draw", userID: 0,
                prevX:snapToGrid(last.x), prevY:snapToGrid(last.y),
                x: snapToGrid(x), y: snapToGrid(y),thickness:1});


        },
        [getPoint, drawLine]
    );

    const pointerUp = useCallback(
        (e:React.PointerEvent<HTMLCanvasElement>) => {
            penDownRef.current = false;
            lastPointRef.current = null;

            //send message
            const {x,y} = getPoint(e);
            sendWebSocketMessage({type: "EndStroke", userID: 0, x: Math.floor(x / GRID_SIZE) * GRID_SIZE, y: Math.floor(y / GRID_SIZE) * GRID_SIZE, thickness: 1});
        },
        []
    );

    const sendUndo = useCallback(
        () => {
            sendWebSocketMessage({type: "Undo", userID: 0});
        },[]
    );
    const sendRedo = useCallback(
        () => {
            sendWebSocketMessage({type: "Redo", userID: 0});
        },[]
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



    const handleSync = useCallback(
        () => {
            //background layer is everything pre-savedstrokes
            //top layer is savedstrokes
            //we just received a affected areas diff

            //apply affected areas diff to the previous image
            //then apply savedstrokes
            //then clear savedstrokes


        },[]
    );

    return(
        <>
            <div className="bg-gray-200">
                <button onClick={connectWebSocket}>connect|</button>
                <button onClick={sendUndo}>undo|</button>
                <button onClick={transferTopToBackground}>transfer</button>
                <div className="relative w-[10px] h-[10px]">
                    <canvas
                        ref={backgroundCanvasRef}
                        width={10}
                        height={10}
                        className=" w-[10px] h-[10px] bg-white image-render-[pixelated] z-10"
                    />
                    <br/>
                    <canvas
                        ref={canvasRef}
                        width={10}
                        height={10}
                        className=" w-[10px] h-[10px] bg-white image-render-[pixelated] z-50"
                        onPointerDown={beginStroke}
                        onPointerMove={pointerMove}
                        onPointerUp={pointerUp}
                    />
                </div>
                <h1>footer</h1>
            </div>
        </>
    )
}

export default Board;