package web;

import model.CanvasTile;
import model.constants.CanvasConstants;
import model.controlActions.ControlAction;
import model.controlActions.Redo;
import model.controlActions.Undo;
import model.helpers.CanvasUtil;
import model.paintActions.*;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import web.helpers.DTO.ServerMessageDTO;
import web.helpers.DTO.UserActionDTO;

import java.util.*;

@Controller
public class STOMPController {

    private static final Object locker = new Object();

    private final SimpMessagingTemplate smt;
    public STOMPController(SimpMessagingTemplate smt){
        this.smt = smt;
    }

    @MessageMapping("/handleaction")
    public void handleTheAction(UserActionDTO action) throws Exception {

        //TODO: when a user sends an uncommitted update (moving a selected region around), update the stateTrackers uncommitted canvas instead

        PaintAction paintAction = switch (action.type) {
            case "BeginStroke" -> new BeginStroke(action.x, action.y, action.thickness, action.r, action.g, action.b, action.a, action.userID);
            case "Draw" -> new Draw(action.prevX, action.prevY, action.x, action.y, action.thickness, action.r, action.g, action.b, action.a, action.userID);
            case "EndStroke" -> new EndStroke(action.x, action.y, action.thickness, action.userID);
            case "BeginErase" -> new BeginErase(action.x, action.y, action.thickness, action.userID);
            case "Erase" -> new Erase(action.prevX, action.prevY, action.x, action.y, action.thickness, action.userID);
            case "EndErase" -> new EndErase(action.x, action.y, action.thickness, action.userID);
            case "Fill" -> new Fill(action.x, action.y, action.r, action.g, action.b, action.a, action.userID);
            default -> null;
        };
        ControlAction controlAction = switch (action.type) {
          case "Undo" -> new Undo(action.userID);
          case "Redo" -> new Redo(action.userID);
          default -> null;
        };

        if(action.type.equals("breakpoint")) PaintServer.stateTracker.debugBreakpoint();
        if(action.type.equals("cleanUp")) PaintServer.stateTracker.cleanTimeline();

        if(paintAction != null){
            PaintServer.stateTracker.receivePaintAction(paintAction);
        }
        if(controlAction != null){
            PaintServer.stateTracker.receiveControlAction(controlAction);
        }
    }

    @EventListener
    public void initialSync(SessionSubscribeEvent subEvent){

        System.out.println("someone subscribed");

        Message<?> isolatedMessage;

        synchronized (locker) {
            isolatedMessage = MessageBuilder.createMessage(
                subEvent.getMessage().getPayload(),
                new MessageHeaders(new HashMap<>(subEvent.getMessage().getHeaders()))
            );
        }

        StompHeaderAccessor headers = StompHeaderAccessor.wrap(isolatedMessage);
        StompCommand command = headers.getCommand();
        if(command == null || headers.getDestination() == null || !headers.getDestination().startsWith("/user") || !command.equals(StompCommand.SUBSCRIBE)) return;

        List<CanvasTile> tileSet = new ArrayList<>();

        for(int ty=0; ty<PaintServer.canvas.getHeight(); ty+= CanvasConstants.TILE_SIDE){
            for(int tx=0; tx<PaintServer.canvas.getWidth(); tx+=CanvasConstants.TILE_SIDE){
                tileSet.add(PaintServer.canvas.getTop().getTile(tx/CanvasConstants.TILE_SIDE,ty/CanvasConstants.TILE_SIDE));
            }
        }

        byte[] imageByteStream = CanvasUtil.tileSetToBytestream(tileSet.toArray(new CanvasTile[0]));

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
        accessor.setContentType(MimeTypeUtils.APPLICATION_OCTET_STREAM);
        accessor.setLeaveMutable(true);
        accessor.setSessionId(headers.getSessionId());
        accessor.setSubscriptionId(headers.getSubscriptionId());

        smt.convertAndSendToUser(Objects.requireNonNull(headers.getSessionId()),"/update/whattoupdate", imageByteStream,accessor.getMessageHeaders());

        SimpMessageHeaderAccessor syncr = SimpMessageHeaderAccessor.create();
        syncr.setSessionId(headers.getSessionId());
        syncr.setLeaveMutable(true);

        int userID=PaintServer.stateTracker.uniqueUsers.get();

        if(headers.containsNativeHeader("sentUserID")){
            if(!headers.getNativeHeader("sentUserID").getFirst().equals("-1")){
                userID = Integer.parseInt(headers.getNativeHeader("sentUserID").getFirst());
                PaintServer.stateTracker.uniqueUsers.getAndDecrement(); //TODO idk about this one
            }
        }

        smt.convertAndSendToUser(headers.getSessionId(),"/update/whattoupdate", new ServerMessageDTO("IDAssignment",userID), syncr.getMessageHeaders());
        PaintServer.stateTracker.uniqueUsers.getAndIncrement();

    }





}
