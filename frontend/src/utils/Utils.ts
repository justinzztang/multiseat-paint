export type Point = { x: number; y: number };

export type WebSocketMessage =
    | {type: "BeginStroke"; userID: number; x: number; y: number}
    | {type: "Draw"; userID: number; prevX:number; prevY:number; x: number; y: number}
    | {type: "EndStroke"; userID: number; x: number; y: number}
    | {type: "Undo"; userID:number}
    | {type: "Redo"; userID:number}