package web.helpers.DTO;

/**
 *export type WebSocketMessage =
 *     | {type: "BeginStroke"; userID: number; x: number; y: number; thickness:number}
 *     | {type: "Draw"; userID: number; prevX:number; prevY:number; x: number; y: number; thickness:number}
 *     | {type: "EndStroke"; userID: number; x: number; y: number; thickness:number}
 */
public class UserActionDTO {
    public String type;
    public int userID;
    public int prevX;
    public int prevY;
    public int x;
    public int y;
    public int thickness;
}
