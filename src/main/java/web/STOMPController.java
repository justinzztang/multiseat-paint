package web;

import model.controlActions.ControlAction;
import model.controlActions.Redo;
import model.controlActions.Undo;
import model.helpers.BoundingBox;
import model.helpers.CanvasUtil;
import model.paintActions.BeginStroke;
import model.paintActions.Draw;
import model.paintActions.EndStroke;
import model.paintActions.PaintAction;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import web.helpers.DTO.ServerMessageDTO;
import web.helpers.DTO.UserActionDTO;

@Controller
public class STOMPController {

    private final SimpMessagingTemplate smt;
    public STOMPController(SimpMessagingTemplate smt){
        this.smt = smt;
    }

    @MessageMapping("/handleaction")
    public void handleTheAction(UserActionDTO action) throws Exception {

        PaintAction paintAction = switch (action.type) {
            case "BeginStroke" -> new BeginStroke(action.x, action.y, action.thickness, action.r, action.g, action.b, action.a, action.userID);
            case "Draw" -> new Draw(action.prevX, action.prevY, action.x, action.y, action.thickness, action.r, action.g, action.b, action.a, action.userID);
            case "EndStroke" -> new EndStroke(action.x, action.y, action.thickness, action.userID);
            default -> null;
        };
        ControlAction controlAction = switch (action.type) {
          case "Undo" -> new Undo(action.userID);
          case "Redo" -> new Redo(action.userID);
          default -> null;
        };

        if(action.type.equals("breakpoint")) PaintServer.stateTracker.debugBreakpoint();

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
        System.out.println(subEvent.toString());
        System.out.println(subEvent.getMessage());

        StompHeaderAccessor headers = StompHeaderAccessor.wrap(subEvent.getMessage());
        StompCommand command = headers.getCommand();
        if(command == null || headers.getDestination() == null || !headers.getDestination().startsWith("/user") || !command.equals(StompCommand.SUBSCRIBE)) return;

        byte[] imageByteStream = CanvasUtil.colorArraySectionToBytestream(PaintServer.canvas.getTop(),0, 0, PaintServer.canvas.getWidth()-1, PaintServer.canvas.getHeight()-1);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
        accessor.setContentType(MimeTypeUtils.APPLICATION_OCTET_STREAM);
        accessor.setLeaveMutable(true);
        accessor.setSessionId(headers.getSessionId());
        accessor.setSubscriptionId(headers.getSubscriptionId());

        Message<byte[]> message = MessageBuilder.createMessage(imageByteStream, accessor.getMessageHeaders());

        smt.convertAndSendToUser(headers.getSessionId(),"/update/whattoupdate", imageByteStream,accessor.getMessageHeaders());

        SimpMessageHeaderAccessor syncr = SimpMessageHeaderAccessor.create();
        syncr.setSessionId(headers.getSessionId());
        syncr.setLeaveMutable(true);
        smt.convertAndSendToUser(headers.getSessionId(),"/update/whattoupdate", new ServerMessageDTO("IDAssignment",PaintServer.stateTracker.uniqueUsers), syncr.getMessageHeaders());
        PaintServer.stateTracker.uniqueUsers++;

    }





}
