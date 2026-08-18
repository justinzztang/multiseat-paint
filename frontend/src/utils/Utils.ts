export type Point = { x: number; y: number };

export type WebSocketMessage =
    | {type: "BeginStroke"; userID: number; x: number; y: number; thickness:number; r:number; g:number; b:number; a:number}
    | {type: "Draw"; userID: number; prevX:number; prevY:number; x: number; y: number; thickness:number; r:number; g:number; b:number; a:number}
    | {type: "EndStroke"; userID: number; x: number; y: number; thickness:number;}
    | {type: "BeginErase"; userID: number; x: number; y: number; thickness:number;}
    | {type: "Erase"; userID: number; prevX:number; prevY:number; x: number; y: number; thickness:number;}
    | {type: "EndErase"; userID: number; x: number; y: number; thickness:number;}
    | {type: "Undo"; userID:number}
    | {type: "Redo"; userID:number}
    | {type: "IDAssignment"; userID:number}
    | {type: "breakpoint"; userID:number}
    | {type: "cleanUp"; userID:number}
    | {type: "ConnectionNotice"; userID:number}



export type Color = {r:number;g:number;b:number;a:number}

export function hexToColor(hex:string):Color | null{

    //thanks google search ai
    let cleanHex = hex.replace(/^#/, '');
    if (cleanHex.length === 3) {
        cleanHex = cleanHex.split('').map(char => char + char).join('');
    }
    if (!/^[0-9a-fA-F]{6}$/.test(cleanHex)) {
        return null;
    }
    const num = parseInt(cleanHex, 16);

    return {
        r: (num >> 16) & 255,
        g: (num >> 8) & 255,
        b: num & 255,
        a: 255
    };
}