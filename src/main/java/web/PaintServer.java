package web;

import model.MemorySmartCanvas;
import model.StateTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class PaintServer {
    public static MemorySmartCanvas canvas = new MemorySmartCanvas(1, 1000,1000);
    public static StateTracker stateTracker = new StateTracker(1, canvas);
    static void main(String[] args) {
        SpringApplication.run(PaintServer.class, args);
    }

    @GetMapping("/canvas")
    public String showCanvas() {
        return canvas.printCanvas();
    }

}