import React, {useCallback, useEffect, useRef, useState} from "react";

import {Client, type IMessage} from "@stomp/stompjs";

import type {Point, WebSocketMessage} from "../utils/Utils.ts";

import {bresenhamLine} from "../utils/Bresenham.ts";

function Board(){

    //websocket stuff
    const stompRef = useRef<Client | null>(null);
    const [status, setStatus] = useState("disconnected");

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

    const receiveWebSocketMessage = useCallback(
        (msg:WebSocketMessage) => {
            switch (msg.type) {
                case "BeginStroke":
                    console.log("received beginstroke");
                    break;
            }
        },
        []
    );

    const connectWebSocket = useCallback(() => {
        setStatus("connecting");

        const client = new Client({
            brokerURL: "ws://localhost:8080/update-websocket",
            reconnectDelay: 5000,
            onConnect: () => {
                setStatus("connected");
                client.subscribe("/update/whattoupdate", (message:IMessage) => {
                    try{
                        const msg = JSON.parse(message.body) as WebSocketMessage;
                        receiveWebSocketMessage(msg);
                    } catch (err){
                        console.error("malformed message:", message.body, err);
                    }
                });
            },
            onDisconnect: () => setStatus("disconnected"),
            onStompError: () => setStatus("error"),
        });

        client.activate();
        stompRef.current = client;

        console.log(status);

    }, [receiveWebSocketMessage]);

    const disconnect = useCallback(() => {
        stompRef.current?.deactivate();
        stompRef.current = null;
    }, []);

    useEffect(() => () => {stompRef.current?.deactivate();}, []);


    const GRID_SIZE = 1;
    var width = 10;
    var height = 10;

    // the canvas
    const canvasRef = useRef<HTMLCanvasElement | null>(null);
    // the canvas context
    const ctxRef = useRef<CanvasRenderingContext2D | null>(null);
    // last point drawn, used for connecting lines smoothly
    const lastPointRef = useRef<Point | null>(null);
    // if the pen is down
    const penDownRef = useRef(false);

    const dpr = window.devicePixelRatio || 1;

    const snap = (v:number) => Math.round(v * dpr) / dpr;

    const snapToGrid = (v:number) => Math.round(v*GRID_SIZE)/GRID_SIZE;

    //init
    useEffect(() => {
        if(!canvasRef.current){
            console.log("ERROR: canvas element not detected");
            return;
        }
        canvasRef.current.width = Math.round(width*dpr);
        canvasRef.current.height = Math.round(height*dpr);

        ctxRef.current = canvasRef.current.getContext("2d");
        if(!ctxRef.current){
            console.log("ERROR: canvas context not detected");
            return;
        }
        ctxRef.current.scale(window.devicePixelRatio, window.devicePixelRatio);
        ctxRef.current.imageSmoothingEnabled = false;
    }, []);

    const getPoint = useCallback( //TODO need a lot of modifications when you add zooming and stuff
        (e: React.PointerEvent<HTMLCanvasElement>) => {
            const rect = e.currentTarget.getBoundingClientRect();
            return { x: e.clientX - rect.left, y: e.clientY - rect.top };
        },[]
    );

    function drawCell(cellX:number,cellY:number,ctx:CanvasRenderingContext2D){
        const left = snap(cellX);
        const top = snap(cellY);
        const right = snap(cellX + GRID_SIZE);
        const bottom = snap(cellY + GRID_SIZE);

        ctx.fillRect(left, top, right - left, bottom - top);
    }


    const beginStroke = useCallback(
        (e: React.PointerEvent<HTMLCanvasElement>) => {
            const { x, y } = getPoint(e);
            const ctx = ctxRef.current;
            if (!ctx) return;
            console.log("beginStroke");
            console.log(x);
            ctx.strokeStyle = "#000000"; //TODO temp color

            const cellX = Math.floor(x / GRID_SIZE) * GRID_SIZE;
            const cellY = Math.floor(y / GRID_SIZE) * GRID_SIZE;

            if(!ctxRef.current) return;
            drawCell(cellX, cellY, ctxRef.current);
            console.log(cellX);

            lastPointRef.current = {x,y};
            penDownRef.current = true;

            //send message
            sendWebSocketMessage({type: "BeginStroke", userID: 0, x: cellX, y: cellY});

        },
        [getPoint]
    );

    const drawLine = useCallback(
        (prevX : number, prevY: number, newX:number, newY:number) => {
            const ctx = ctxRef.current;
            if (!ctx) return;
            ctx.strokeStyle="#000000"; //TODO temp color
            const markedCells = bresenhamLine(prevX,prevY,newX,newY);
            for (const cell of markedCells) {
                drawCell(cell.x , cell.y, ctx);
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
                x: snapToGrid(x), y: snapToGrid(y)});


        },
        [getPoint, drawLine]
    );

    const pointerUp = useCallback(
        (e:React.PointerEvent<HTMLCanvasElement>) => {
            penDownRef.current = false;
            lastPointRef.current = null;

            //send message
            const {x,y} = getPoint(e);
            sendWebSocketMessage({type: "EndStroke", userID: 0, x: Math.floor(x / GRID_SIZE) * GRID_SIZE, y: Math.floor(y / GRID_SIZE) * GRID_SIZE});
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

    return(
        <>
            <button onClick={connectWebSocket}>connect</button>
            <button onClick={sendUndo}>undo</button>
            <button onClick={sendRedo}>redo</button>
            <canvas
                ref={canvasRef}
                className="w-[10px] h-[10px] bg-cyan-100 image-render-[pixelated]" onPointerDown={beginStroke} onPointerMove={pointerMove} onPointerUp={pointerUp}
            />
        </>
    )
}

export default Board;