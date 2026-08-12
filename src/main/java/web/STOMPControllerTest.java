package web;

import model.controlActions.ControlAction;
import model.controlActions.Redo;
import model.controlActions.Undo;
import model.paintActions.BeginStroke;
import model.paintActions.Draw;
import model.paintActions.EndStroke;
import model.paintActions.PaintAction;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;
import web.helpers.DTO.StrokeDTO;
import web.helpers.DTO.TempDTO;
import web.helpers.DTO.TempDTO2;

@Controller
public class STOMPControllerTest {

    @MessageMapping("/handleaction")
    @SendTo("/update/whattoupdate") //send data to everyone connected to /update/whattoupdate
    public void handleTheAction(StrokeDTO action) throws Exception {

        PaintAction paintAction = switch (action.type) {
            case "BeginStroke" -> new BeginStroke(action.x, action.y, action.userID);
            case "Draw" -> new Draw(action.prevX, action.prevY, action.x, action.y, action.userID);
            case "EndStroke" -> new EndStroke(action.x, action.y, action.userID);
            default -> null;
        };
        ControlAction controlAction = switch (action.type) {
          case "Undo" -> new Undo(action.userID);
          case "Redo" -> new Redo(action.userID);
          default -> null;
        };

        if(paintAction != null){
            PaintServer.stateTracker.receivePaintAction(paintAction);
        }
        if(controlAction != null){
            PaintServer.stateTracker.receiveControlAction(controlAction);
        }
    }


}
