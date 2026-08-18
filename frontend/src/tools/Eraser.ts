import {Brush} from "./Brush.ts";

export class Eraser extends Brush{
    name = "Eraser";
    constructor(thickness:number){
        super(thickness);
    }
}