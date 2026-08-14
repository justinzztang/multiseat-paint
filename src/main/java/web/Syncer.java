package web;

import model.helpers.BoundingBox;
import model.helpers.CanvasUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;


@Service
public class Syncer {


    private final SimpMessagingTemplate smt;

    public Syncer(SimpMessagingTemplate smt){
        this.smt = smt;
    }

    @Scheduled(fixedRate= 100)
    public void syncCanvas(){

        //statetracker or something stores the affected area
        //send to the clients for them to calculate the canvas themselves

        //System.out.println("this sync was at:" + PaintServer.stateTracker.lastSyncIndex);
        BoundingBox aabb = PaintServer.stateTracker.affectedAreaBoundingBox(true);
        byte[] imageByteStream = CanvasUtil.colorArraySectionToBytestream(PaintServer.canvas.getTop(),aabb.minX, aabb.minY, aabb.maxX, aabb.maxY); //TODO theres a bug where its going based off the last 2 strokes, instead of just 1

        /*System.out.println("=====");
        System.out.println(aabb.minX);
        System.out.println(aabb.minY);
        System.out.println(aabb.maxX);
        System.out.println(aabb.maxY);
        System.out.println("=====");*/



        //thanks claude
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
        accessor.setContentType(MimeTypeUtils.APPLICATION_OCTET_STREAM);
        accessor.setLeaveMutable(true);

        Message<byte[]> message = MessageBuilder.createMessage(imageByteStream, accessor.getMessageHeaders());

        //smt.convertAndSend("/update/whattoupdate", new CanvasDTO(PaintServer.canvas.getTop()));
        smt.send("/update/whattoupdate", message);
    }

}
