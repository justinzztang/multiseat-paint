import type {Tool} from "./Tool.ts";

export abstract class Brush implements Tool {
    abstract name: string;
    thickness: number;
    protected constructor(thickness:number){
        this.thickness = thickness;
    }
}