import type {Point} from "./Utils.ts";

//https://en.wikipedia.org/wiki/Bresenham's_line_algorithm
export function bresenhamLine(prevX : number, prevY:number, newX:number, newY:number): Point[]{

    const points: Point[] = [];

    let x0 = Math.round(prevX);
    let y0 = Math.round(prevY);
    const x1 = Math.round(newX);
    const y1 = Math.round(newY);

    const dx = Math.abs(x1 - x0);
    const sx = x0 < x1 ? 1 : -1;
    const dy = Math.abs(y1 - y0);
    const sy = y0 < y1 ? 1 : -1;

    let error = dx - dy;

    while (true) {

        points.push({ x: x0, y: y0 });

        const e2 = 2 * error;

        if (x0 === x1 && y0 === y1) {
            break;
        }

        if (e2 > -dy) {
            error -= dy;
            x0 += sx;
        }

        if (e2 < dx) {
            error += dx;
            y0 += sy;
        }
    }

    return points;

}