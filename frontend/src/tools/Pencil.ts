import type {Color} from "../utils/Utils.ts";
import {Brush} from "./Brush.ts";
export class Pencil extends Brush {
    name = "Pencil";
    penColor:Color;
    constructor(penColor:Color, thickness:number){
        super(thickness);
        this.penColor = penColor;
    }
}