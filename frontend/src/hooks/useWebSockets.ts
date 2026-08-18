import { useRef, useCallback, useEffect, useState } from 'react';
import type { WebSocketMessage } from "./Utils.ts";
import {Client} from "@stomp/stompjs";

export function useWebSockets(url: string) {

    const stompRef = useRef<Client | null>(null);
    const [status, setStatus] = useState("disconnected");
    const [userID, setUserID] = useState(-1);

    const sendWebSocketMessage = useCallback(
        (msg:WebSocketMessage) => {
            const client = stompRef.current;
            if (client?.connected){
                client.publish({
                    destination: "/app/handleaction",
                    body: JSON.stringify(msg)
                })
            }
        },
        []
    );

    const receiveWebSocketMessage = useCallback(
        (msg:WebSocketMessage) => {
            switch (msg.type) {
                case "BeginStroke":
                    console.log("received beginstroke");
                    break;
                case "IDAssignment":
                    console.log("assigned user id: " + msg.userID);
                    setUserID(msg.userID);
                    break;
                default:
                    console.log("received some json thing");
            }
        },
        []
    );


    const wsRef = useRef<WebSocket | null>(null);
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        const ws = new WebSocket(url);
        wsRef.current = ws;

        ws.onopen = () => setIsConnected(true);
        ws.onclose = () => setIsConnected(false);
        ws.onmessage = (event) => onMessage(JSON.parse(event.data));

        return () => ws.close();
    }, [url, onMessage]);

    const sendMessage = useCallback((msg: WSMessage) => {
        if (wsRef.current?.readyState === WebSocket.OPEN) {
            wsRef.current.send(JSON.stringify(msg));
        }
    }, []);

    return { isConnected, sendMessage };
}