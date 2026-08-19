export type Point = { x: number; y: number };

export type WebSocketMessage =
    | {type: "BeginStroke"; userID: number; x: number; y: number; thickness:number; r:number; g:number; b:number; a:number}
    | {type: "Draw"; userID: number; prevX:number; prevY:number; x: number; y: number; thickness:number; r:number; g:number; b:number; a:number}
    | {type: "EndStroke"; userID: number; x: number; y: number; thickness:number;}
    | {type: "BeginErase"; userID: number; x: number; y: number; thickness:number;}
    | {type: "Erase"; userID: number; prevX:number; prevY:number; x: number; y: number; thickness:number;}
    | {type: "EndErase"; userID: number; x: number; y: number; thickness:number;}
    | {type: "Fill"; userID: number; x: number; y: number; r:number; g:number; b:number; a:number}
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

export function colorToHex(c:Color):string{

    //thanks gemini
    return '#' + Array.from([c.r, c.g, c.b]) //TODO transparency doesnt work
        .map(val => val.toString(16).padStart(2, '0'))
        .join('')
}

export function transparentOnWhite(c:Color ):Color{

    let fgR = c.r * c.a;
    let newR = fgR*255 + 255*255 * (255 - c.a);
    newR = newR / 255 / 255;

    let fgG = c.g * c.a;
    let newG = fgG*255 + 255*255 * (255 - c.a);
    newG = newG / 255 / 255;

    let fgB = c.b * c.a;
    let newB = fgB*255 + 255*255 * (255 - c.a);
    newB = newB / 255 / 255;

    return {r:newR, g:newG, b:newB, a:255};
}
