import React, {useRef, useCallback} from "react";
import type {Point, Color, CanvasProperties} from "../utils/Utils.ts";
import {drawCell, getPoint} from "../utils/CanvasUtils.ts";
import {filledCircle} from "../utils/Circles.ts";
import type {Tool} from "../tools/Tool.ts";
import {Brush} from "../tools/Brush.ts";
import {Pencil} from "../tools/Pencil.ts";
import {Eraser} from "../tools/Eraser.ts";


export function useDrawing(tool:Tool, canvasSettings:CanvasProperties){

    const GRID_SIZE = canvasSettings.GRID_SIZE;
    const width = canvasSettings.WIDTH;
    const height = canvasSettings.HEIGHT;

    const toolRef = useRef(tool);

    // the top canvas
    const canvasRef = useRef<HTMLCanvasElement | null>(null); //gets set by the page
    // the top canvas context
    const ctxRef = useRef<CanvasRenderingContext2D | null>(null);
    //the background canvas
    const backgroundCanvasRef = useRef <HTMLCanvasElement | null>(null); //gets set by the page
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

    const colorRef = useRef<HTMLInputElement | null>(null); //gets set by the page

    const erasingRef = useRef(false);

    const thicknessRef = useRef(1);

    const dpr = window.devicePixelRatio || 1;


    //handle brush objects
    const brushStroke = useCallback(
        (e:React.PointerEvent<HTMLCanvasElement>) => {
            //get point from click event
            //get a filled circle around the click
            //draw the filled circle
            //update the last point
            //update pendown
            if(!(toolRef.current instanceof Brush)){
                return; //shouldn't happen
            }

            const {x, y} = getPoint(e);
            const ctx = ctxRef.current;
            if (!ctx) return;

            const cellX = Math.floor(x / GRID_SIZE) * GRID_SIZE;
            const cellY = Math.floor(y / GRID_SIZE) * GRID_SIZE;

            const markedPoints = filledCircle(cellX, cellY, toolRef.current.thickness);
            for(const point of markedPoints){

                //TODO replace with something like toolRef.current.apply();

                let snappedX = Math.round(point.x * dpr) / dpr;
                let snappedY = Math.round(point.y * dpr) / dpr

                if(toolRef.current instanceof Pencil){
                    drawCell(snappedX, snappedY,GRID_SIZE, ctx, toolRef.current.penColor);
                }
                else if(toolRef.current instanceof Eraser){
                    drawCell(snappedX, snappedY,GRID_SIZE, ctx, {r:255,g:255,b:255,a:255}); //TODO figure out how to do transparent erase/background color
                }

            }

            lastPointRef.current = {x,y};
            penDownRef.current = true;





        },[]
    );

    const onClick = useCallback(
        (e:React.PointerEvent<HTMLCanvasElement>) => {
            //router
            if(toolRef.current instanceof Brush){
                brushStroke(e);
            }
        },[brushStroke]
    );


}