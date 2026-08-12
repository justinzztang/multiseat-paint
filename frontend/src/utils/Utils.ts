export type Point = { x: number; y: number };

export type WebSocketMessage =
    | {type: "BeginStroke"; userID: number; x: number; y: number; thickness:number}
    | {type: "Draw"; userID: number; prevX:number; prevY:number; x: number; y: number; thickness:number}
    | {type: "EndStroke"; userID: number; x: number; y: number; thickness:number}
    | {type: "Undo"; userID:number}
    | {type: "Redo"; userID:number}


export type Color = {r:number;g:number;b:number;a:number}