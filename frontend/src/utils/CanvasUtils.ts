import React, {useCallback} from "react";
import type {Color} from "./Utils.ts";

export const getPoint = useCallback( //TODO need a lot of modifications when you add zooming and stuff
    (e: React.PointerEvent<HTMLCanvasElement>) => {
        const rect = e.currentTarget.getBoundingClientRect();
        return { x: e.clientX - rect.left, y: e.clientY - rect.top };
    },[]
);

/** cellX, cellY should be snapped */
export function drawCell(cellX:number,cellY:number,gridSize:number, ctx:CanvasRenderingContext2D, color:Color){
    const left = cellX;
    const top = cellY;
    const right = cellX + gridSize;
    const bottom = cellY + gridSize;

    ctx.fillStyle = `rgba(${color.r}, ${color.g}, ${color.b}, ${color.a})`;
    ctx.fillRect(left, top, right - left, bottom - top);
}
