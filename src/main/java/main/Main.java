package main;

import model.*;
import model.controlActions.Redo;
import model.controlActions.Undo;
import model.paintActions.BeginStroke;
import model.paintActions.Draw;
import model.paintActions.EndStroke;

public class Main {
    static void main(String[] args) {
        COWTileCanvas canvas = new COWTileCanvas(1, 10, 10);
        canvas.setPixel(0,0,123,123,123,255);
        canvas.setPixel(7,5,123,123,123,255);
        canvas.setPixel(9,9,123,123,123,255);
        System.out.println(canvas);
        canvas.copyTopLayer();

        var c = canvas.getLayerCopy(0);
        System.out.println("end of debug");

    }
}