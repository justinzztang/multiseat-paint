package web;

import model.CanvasTile;
import model.helpers.CanvasUtil;
import model.helpers.MemoryEmergency;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;


@Service
public class Syncer {

    private final WebSocketMessageBrokerStats stats;
    private final SimpMessagingTemplate smt;

    public Syncer(SimpMessagingTemplate smt, WebSocketMessageBrokerStats stats){
        this.stats = stats;
        this.smt = smt;
    }

    @Scheduled(fixedRate= 200)
    public void syncCanvas(){

        //TODO: send out sync messages regarding the uncommitted canvas

        PaintServer.stateTracker.stateLock.readLock().lock();
        PaintServer.stateTracker.stateLock.readLock().unlock();

        CanvasTile[] affectedTiles = PaintServer.stateTracker.affectedAreaTiles(true); //it seems that this is working
        byte[] imageByteStream = CanvasUtil.tileSetToBytestream(affectedTiles); //TODO theres a bug where its going based off the last 2 strokes, instead of just 1

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
        accessor.setContentType(MimeTypeUtils.APPLICATION_OCTET_STREAM);
        accessor.setLeaveMutable(true);

        Message<byte[]> message = MessageBuilder.createMessage(imageByteStream, accessor.getMessageHeaders());

        smt.send("/update/whattoupdate", message);



    }

    @Scheduled(fixedRate= 10000)
    public void cleanup(){

        try{
            int ac = Objects.requireNonNull(stats.getWebSocketSessionStats()).getWebSocketSessions();
            PaintServer.stateTracker.activeConnections.set(ac);
        }
        catch(Exception _){
            //just dont update
        }

        PaintServer.stateTracker.cleanTimeline(false);

    }

    @Scheduled(fixedRate=30000)
    public void runGarbageCollection(){
        if(MemoryEmergency.memoryEmergency()){
            PaintServer.stateTracker.cleanTimeline(true);
            System.out.println("Memory limit reached! Cleaning timeline...");
        }
    }

}
