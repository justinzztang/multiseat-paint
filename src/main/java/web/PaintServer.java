package web;

import model.CanvasImpl;
import model.MemorySmartCanvas;
import model.StateTracker;
import web.helpers.DTO.StrokeDTO;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.handler.annotation.MessageMapping;

@SpringBootApplication
@RestController
public class PaintServer {
    public static MemorySmartCanvas canvas = new MemorySmartCanvas(1, 10,10);
    public static StateTracker stateTracker = new StateTracker(1, canvas);
    static void main(String[] args) {
        SpringApplication.run(PaintServer.class, args);
    }

    @GetMapping("/canvas")
    public String showCanvas() {
        return canvas.printCanvas();
    }

}