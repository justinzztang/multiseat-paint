import React, {useCallback, useEffect, useRef, useState} from "react";

import {Client, type IMessage} from "@stomp/stompjs";

import {type Point, type WebSocketMessage, type Color, transparentOnWhite, colorToHex} from "../utils/Utils.ts";

import {hexToColor} from "../utils/Utils.ts";

import {bresenhamLine} from "../utils/Bresenham.ts";

import {filledCircle, thickCircle} from "../utils/Circles.ts";


function Board(){

    const GRID_SIZE = 1;
    const width = 1000;
    const height = 1000;

    // the top canvas
    const canvasRef = useRef<HTMLCanvasElement | null>(null);
    // the top canvas context
    const ctxRef = useRef<CanvasRenderingContext2D | null>(null);
    //the background canvas
    const backgroundCanvasRef = useRef <HTMLCanvasElement | null>(null);
    //the background canvas context
    const backgroundCtxRef = useRef<CanvasRenderingContext2D | null>(null);

    // the canvas used for making the putimagedata syncs normal
    const syncCanvasRef = useRef<OffscreenCanvas>(new OffscreenCanvas(1,1));
    // the sync canvas context
    const syncCtxRef = useRef<OffscreenCanvasRenderingContext2D>(syncCanvasRef.current.getContext("2d"));

    // the canvas used for making drawLine fast
    const lineCanvasRef = useRef<OffscreenCanvas>(new OffscreenCanvas(1,1));
    // the line canvas context
    const lineCtxRef = useRef<OffscreenCanvasRenderingContext2D>(lineCanvasRef.current.getContext("2d"));


    // last point drawn, used for connecting lines smoothly
    const lastPointRef = useRef<Point | null>(null);
    // if the pen is down
    const penDownRef = useRef(false);

    const [color, setColor] = useState("#000000");

    const [toolName, setToolName] = useState("Pencil");

    const [thickness, setThickness] = useState(1);

    const dpr = window.devicePixelRatio || 1;

    const snap = (v:number) => Math.round(v * dpr) / dpr;

    const snapToGrid = (v:number) => Math.round(v*GRID_SIZE)/GRID_SIZE;

    useEffect(() => {
        // @ts-ignore
        setColor(getRandomHexColor());

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
            if(toolName !== "Eraser" && toolName !== "Pencil") return; //TODO more robust system when i have more time
            const ctx = ctxRef.current;
            if (!ctx) return;
            const markedCells = bresenhamLine(prevX,prevY,newX,newY);
            let penColor = hexToColor(color);
            if (!penColor) penColor = {r:0,g:0,b:0,a:255};

            if(lineCanvasRef.current.width !== width || lineCanvasRef.current.height !== height){
                lineCanvasRef.current.width = width;
                lineCanvasRef.current.height = height;
            }

            const imageData = ctx.createImageData(width, height);
            const data = imageData.data;

            for (const cell of markedCells) {
                const markedPoints = thickCircle(cell.x, cell.y, thickness);
                for(const point of markedPoints){
                    if(point.x < 0 || point.x >= width || point.y < 0 || point.y >= height) continue;
                    if(toolName === "Eraser"){//TODO handle erasing using transparency or background color
                        data[point.y*width*4 + point.x*4] = 255;
                        data[point.y*width*4 + point.x*4+1] = 255;
                        data[point.y*width*4 + point.x*4+2] = 255;
                        data[point.y*width*4 + point.x*4+3] = 255;
                    }
                    else if(toolName === "Pencil"){
                        data[point.y*width*4 + point.x*4] = penColor.r;
                        data[point.y*width*4 + point.x*4+1] = penColor.g;
                        data[point.y*width*4 + point.x*4+2] = penColor.b;
                        data[point.y*width*4 + point.x*4+3] = penColor.a;
                    }
                }
            }

            // @ts-ignore
            lineCtxRef.current.putImageData(
                imageData, 0, 0
            );
            ctx.drawImage(lineCanvasRef.current,0,0, width, height, 0, 0, width, height);

        },[drawCell, bresenhamLine, thickness, toolName]
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
    const [status, setStatus] = useState("Not Connected");
    const [userID, setUserID] = useState(-1);

    const sendWebSocketMessage = useCallback(
        (msg:WebSocketMessage) => {
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
                case "IDAssignment":
                    console.log("Assigned user ID: " + msg.userID);
                    setUserID(msg.userID);
                    break;
                default:
                    console.log("Received some unrecognized json message");
            }
        },
        []
    );

    const receiveSyncMessage = useCallback(
        (msg:IMessage) => {
            const bg = backgroundCtxRef.current;
            if (!bg) return;

            const bytes:Uint8Array = msg.binaryBody;
            const tiles = (bytes[0] << 8) + (bytes[1]);

            if(syncCanvasRef.current.width !== width || syncCanvasRef.current.height !== height){
                syncCanvasRef.current.width = width;
                syncCanvasRef.current.height = height;
            }

            let index = 2;
            for(let i=0;i<tiles;i++){
                //the next 8 bytes are metadata
                const startingX = (bytes[index] << 8) + (bytes[index+1]);
                const startingY = (bytes[index+2] << 8) + (bytes[index+3]);
                const width = (bytes[index+4] << 8) + (bytes[index+5]);
                const height = (bytes[index+6] << 8) + (bytes[index+7]);

                index +=8;

                const imageData = bg.createImageData(width, height);
                const data = imageData.data;
                //the next width*height*4 bytes are colordata
                data.set(bytes.subarray(index, index + width*height*4));
                // @ts-ignore
                syncCtxRef.current.putImageData(imageData,startingX,startingY);

                bg.clearRect(startingX,startingY,width,height);
                bg.drawImage(
                    syncCanvasRef.current,
                    startingX, startingY, width, height,
                    startingX, startingY, width, height
                );

                index+=width*height*4;

            }
            transferTopToBackground();
        },
        [transferTopToBackground]
    );

    //thanks gemini
    const getRandomHexColor = (): string =>
        `#${Math.floor(Math.random() * 16777215).toString(16).padStart(6, '0')}`;

    const buildURL = (): string => {
        if (import.meta.env.VITE_WS_URL) return import.meta.env.VITE_WS_URL;
        const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
        const host = import.meta.env.DEV
            ? `${window.location.hostname}:8080`
            : window.location.host;
        return `${protocol}//${host}/update-websocket`;
    };

    //problem
    const connectWebSocket = useCallback(() => {
        setStatus("Connecting...");

        const client = new Client({
            brokerURL: buildURL(),
            reconnectDelay: 5000,
            //debug: (str) => console.log("[stomp]", str),
            onConnect: () => {
                setStatus("Connected");
                //its gotta be here

                // @ts-ignore
                backgroundCtxRef.current.clearRect(0,0,backgroundCanvasRef.current.width,backgroundCanvasRef.current.height);
                // @ts-ignore
                ctxRef.current.clearRect(0,0,backgroundCanvasRef.current.width,backgroundCanvasRef.current.height);

                client.subscribe("/update/whattoupdate", messageCallback);

                client.subscribe("/user/update/initialsync", messageCallback,{"sentUserID":userID.toString()});

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

            },
            onWebSocketError: () => {
                setStatus("Reconnecting...");
            }
        });

        client.activate();
        stompRef.current = client;

        console.log(status);

    }, [receiveWebSocketMessage, receiveSyncMessage, userID]);

    const disconnect = useCallback(() => {
        console.log("disconnected")
        setStatus("Not Connected")
        stompRef.current?.deactivate();
        stompRef.current = null;
    }, []);

    useEffect(() => () => {stompRef.current?.deactivate();}, []);

    const beginStroke = useCallback(
        (e: React.PointerEvent<HTMLCanvasElement>) => {

            if(toolName === "Eyedropper"){
                const { x, y } = getPoint(e);
                // @ts-ignore
                const pixelData = backgroundCtxRef.current.getImageData(x*dpr,y*dpr,1,1).data;


                const newColor = transparentOnWhite({r:pixelData[0], g:pixelData[1], b:pixelData[2], a:pixelData[3]});

                setColor(colorToHex(newColor));

                return;
            }
            else if(toolName === "Fill"){
                const { x, y } = getPoint(e);
                const cellX = Math.floor(x / GRID_SIZE) * GRID_SIZE;
                const cellY = Math.floor(y / GRID_SIZE) * GRID_SIZE;
                let penColor = hexToColor(color);
                if (!penColor) penColor = {r:0,g:0,b:0,a:255};

                sendWebSocketMessage({
                    type: "Fill",
                    userID: userID,
                    x: cellX,
                    y: cellY,
                    r: penColor.r,
                    g: penColor.g,
                    b: penColor.b,
                    a: penColor.a
                });
                return;
            }

            const { x, y } = getPoint(e);
            const ctx = ctxRef.current;
            if (!ctx) return;

            const cellX = Math.floor(x / GRID_SIZE) * GRID_SIZE;
            const cellY = Math.floor(y / GRID_SIZE) * GRID_SIZE;

            if(!ctxRef.current) return;
            let penColor = hexToColor(color);
            if (!penColor) penColor = {r:0,g:0,b:0,a:255};

            const markedPoints = filledCircle(cellX, cellY, thickness);
            for(const point of markedPoints){
                if(toolName==="Eraser"){
                    drawCell(point.x, point.y, ctxRef.current, {r:255,g:255,b:255,a:255}); //TODO figure out how to do transparent erase/background color
                }
                else if (toolName==="Pencil"){
                    drawCell(point.x, point.y, ctxRef.current, penColor);
                }
            }
            //drawCell(cellX, cellY, ctxRef.current, penColor);
            //console.log(cellX);

            lastPointRef.current = {x,y};
            penDownRef.current = true;

            //send message
            if(toolName === "Eraser"){
                sendWebSocketMessage({
                    type: "BeginErase",
                    userID: userID,
                    x: cellX,
                    y: cellY,
                    thickness: thickness,
                });
            }
            else if (toolName === "Pencil"){
                sendWebSocketMessage({
                    type: "BeginStroke",
                    userID: userID,
                    x: cellX,
                    y: cellY,
                    thickness: thickness,
                    r: penColor.r,
                    g: penColor.g,
                    b: penColor.b,
                    a: penColor.a
                });
            }
        },
        [drawCell, getPoint, sendWebSocketMessage, userID, thickness,toolName]
    );



    const pointerMove = useCallback(
        (e:React.PointerEvent<HTMLCanvasElement>) =>{
            if(toolName !== "Eraser" && toolName !== "Pencil") return; //TODO more robust system when i have more time
            if (!lastPointRef.current || !penDownRef.current) return;
            const {x,y} = getPoint(e);
            const last = lastPointRef.current;
            drawLine(last.x, last.y, x, y);
            lastPointRef.current = {x,y};

            let penColor = hexToColor(color);
            if (!penColor) penColor = {r:0,g:0,b:0,a:255};

            //send message
            if(toolName==="Eraser"){
                sendWebSocketMessage({
                    type: "Erase",
                    userID: userID,
                    prevX: snapToGrid(last.x),
                    prevY: snapToGrid(last.y),
                    x: snapToGrid(x),
                    y: snapToGrid(y),
                    thickness: thickness,
                });
            }
            else if(toolName==="Pencil"){
                sendWebSocketMessage({
                    type: "Draw",
                    userID: userID,
                    prevX: snapToGrid(last.x),
                    prevY: snapToGrid(last.y),
                    x: snapToGrid(x),
                    y: snapToGrid(y),
                    thickness: thickness,
                    r: penColor.r,
                    g: penColor.g,
                    b: penColor.b,
                    a: penColor.a
                });
            }

        },
        [getPoint, drawLine, sendWebSocketMessage, userID, thickness,toolName]
    );

    const pointerUp = useCallback(
        (e:React.PointerEvent<HTMLCanvasElement>) => {
            if(toolName !== "Eraser" && toolName !== "Pencil") return; //TODO more robust system when i have more time

            if(!penDownRef.current) return;

            penDownRef.current = false;
            lastPointRef.current = null;

            //send message
            const {x,y} = getPoint(e);
            if(toolName==="Eraser"){
                sendWebSocketMessage({
                    type: "EndErase",
                    userID: userID,
                    x: Math.floor(x / GRID_SIZE) * GRID_SIZE,
                    y: Math.floor(y / GRID_SIZE) * GRID_SIZE,
                    thickness: thickness
                });
            }
            else if(toolName==="Pencil"){
                sendWebSocketMessage({
                    type: "EndStroke",
                    userID: userID,
                    x: Math.floor(x / GRID_SIZE) * GRID_SIZE,
                    y: Math.floor(y / GRID_SIZE) * GRID_SIZE,
                    thickness: thickness
                });
            }
        },
        [getPoint, sendWebSocketMessage, userID, thickness,toolName]
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

    /*const breakpoint = useCallback(
        () => {
            sendWebSocketMessage({type: "breakpoint", userID: userID});
        },[ sendWebSocketMessage, userID]
    );

    const cleanUp = useCallback(
        () => {
            sendWebSocketMessage({type: "cleanUp", userID: userID});
        },[ sendWebSocketMessage, userID]
    );*/

    const thicknessSlider = useCallback(
        (e:React.ChangeEvent<HTMLInputElement>) => {
            setThickness(e.currentTarget.valueAsNumber);
        },[thickness]
    );

    return(
        <>
            <div className=" p-6 bg-slate-100">

                    <div className="flex flex-col">
                        <div className="flex justify-center">
                            <div className="flex justify-start overflow-x-auto border bg-white">
                                <div className={"flex divide-x"}>
                                    <button disabled={true} className="w-[150px] h-[50px]">{userID===-1 ? "Click Connect ->" : `Your ID is: ${userID}`}</button>
                                    <button disabled={status!=="Not Connected"} onClick={connectWebSocket} className="w-[150px] h-[50px] disabled:cursor-not-allowed">Connect</button>
                                    <button disabled={status==="Not Connected"} onClick={disconnect} className="w-[150px] h-[50px] disabled:cursor-not-allowed">Disconnect</button>
                                    <button className="w-[200px] h-[50px]">{status}</button>
                                    {/*<button onClick={breakpoint}>breakpoint</button>*/}
                                    {/*<button onClick={cleanUp}>clean</button>*/}
                                </div>
                            </div>
                        </div>
                        <br/>
                        <div className="flex justify-center">
                            <div className = "flex overflow-x-auto justify-start divide-x bg-white">
                                <div className = "flex flex-col divide-y divide-black border-t border-l border-b">
                                    <div className = "flex divide-x divide-black">
                                        <button className={`w-[75px] h-[75px] ${toolName === "Pencil" ? "bg-slate-200" : ""}`} onClick={() => setToolName("Pencil")}>Pencil</button>
                                        <button className={`w-[75px] h-[75px] ${toolName === "Eraser" ? "bg-slate-200" : ""}`} onClick={() => setToolName("Eraser")}>Eraser</button>
                                        <button className="w-[75px] h-[75px]" onClick={sendUndo}>Undo</button>
                                    </div>
                                    <div className = "flex divide-x divide-black">
                                        <button className={`w-[75px] h-[75px] ${toolName === "Eyedropper" ? "bg-slate-200" : ""}`} onClick={() => setToolName("Eyedropper")}>Color Selector</button>
                                        <button className={`w-[75px] h-[75px] ${toolName === "Fill" ? "bg-slate-200" : ""}`} onClick={() => setToolName("Fill")}>Fill</button>
                                        <button className="w-[75px] h-[75px]" onClick={sendRedo}>Redo</button>
                                    </div>
                                </div>

                                <div className = "flex-col items-center justify-center w-[150px] h-[153px] border-t border-b">
                                    <div className="grid place-items-center">
                                        <button className="h-[25px]">{thickness}px</button>
                                        <div className={"w-[100px] h-[100px] flex items-center justify-center"}>
                                            <button style={{ width: `${thickness}px`, height: `${thickness}px`, backgroundColor: color}} className="bg-pink-200 rounded-full"></button>
                                        </div>
                                        <input className="h-[25px]" type="range" min={1} max={100} step={1} value={thickness} onChange={thicknessSlider}/>
                                    </div>
                                </div>

                                <div className = "flex flex-col divide-y divide-black border-t border-b">
                                    <div className = "flex divide-x divide-black">
                                        <button className="bg-[#000000] w-[75px] h-[75px]" onClick={()=> setColor("#000000")}></button>
                                        <button className="bg-[#ED1C24] w-[75px] h-[75px]" onClick={()=> setColor("#ED1C24")}></button>
                                        <button className="bg-[#FFF200] w-[75px] h-[75px]" onClick={()=> setColor("#FFF200")}></button>
                                        <button className="bg-[#00A2E8] w-[75px] h-[75px]" onClick={()=> setColor("#00A2E8")}></button>
                                        <button className="bg-[#3F48CC] w-[75px] h-[75px]" onClick={()=> setColor("#3F48CC")}></button>
                                    </div>
                                    <div className = "flex divide-x divide-black">
                                        <button className="bg-[#FFFFFF] w-[75px] h-[75px]" onClick={()=> setColor("#FFFFFF")}></button>
                                        <button className="bg-[#FFAEC9] w-[75px] h-[75px]" onClick={()=> setColor("#FFAEC9")}></button>
                                        <button className="bg-[#FFC90E] w-[75px] h-[75px]" onClick={()=> setColor("#FFC90E")}></button>
                                        <button className="bg-[#22B14C] w-[75px] h-[75px]" onClick={()=> setColor("#22B14C")}></button>
                                        <button className="bg-[#A349A4] w-[75px] h-[75px]" onClick={()=> setColor("#A349A4")}></button>
                                    </div>
                                </div>
                                <div className = "flex flex-col items-center justify-center w-[75px] h-[153px] border-t border-r border-b">
                                    <div className="grid place-items-center h-max">
                                        <button className="h-[25px]">Custom:</button>
                                        <input className="w-[75px] h-[75px]" type="color" value={color} id="strokeColor" name="strokeColor" onChange={(e) => setColor(e.target.value)}></input>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className={"flex items-center justify-center"}>
                        <div className="relative mt-10" style={{ width: `${width}px`, height: `${height}px` }}>
                            <canvas
                                ref={backgroundCanvasRef}
                                className="absolute top-0 left-0 bg-white image-render-[pixelated] z-10"
                                style={{ width: `${width}px`, height: `${height}px` }}
                            />
                            <canvas
                                ref={canvasRef}
                                className="absolute top-0 left-0 image-render-[pixelated] z-50"
                                style={{ width: `${width}px`, height: `${height}px`, pointerEvents: status==="Connected" ? "auto" : "none"}}
                                onPointerDown={beginStroke}
                                onPointerMove={pointerMove}
                                onPointerUp={pointerUp}
                                onPointerOut={pointerUp}
                            />
                        </div>
                    </div>
                    <h1>Version 1.0.0</h1>
            </div>
        </>
    )
}

export default Board;