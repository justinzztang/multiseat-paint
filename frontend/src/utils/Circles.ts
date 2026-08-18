import type {Point} from "./Utils.ts";

//https://en.wikipedia.org/wiki/Midpoint_circle_algorithm
export function midpointCircle(x:number, y:number, r:number): Point[]{

    x = Math.trunc(x);
    y = Math.trunc(y);
    r = Math.trunc(r);

    const points = new Set<Point>();
    let t1 = Math.trunc(r / 16);
    let x1 = r;
    let y1 = 0;

    while(x1 >= y1){

        points.add({x:x1, y:y1});
        points.add({x:-x1, y:y1});
        points.add({x:x1, y:-y1});
        points.add({x:-x1, y:-y1});
        points.add({x:y1, y:x1});
        points.add({x:y1, y:-x1});
        points.add({x:-y1, y:x1});
        points.add({x:-y1, y:-x1});

        y1++;
        t1 += y1;
        let t2 = t1 - x1;
        if(t2>=0){
            t1 = t2;
            x1--;
        }
    }

    points.forEach(point => {
        point.x += x;
        point.y += y;
    });

    return Array.from(points);

}

//https://stackoverflow.com/questions/35801952/drawing-concentric-tiling-circles-with-even-diameter
export function evenDiameterCircle(x:number, y:number, r:number): Point[]{
    x = Math.trunc(x);
    y = Math.trunc(y);
    r = Math.trunc(r);

    const points = new Set<Point>();
    let x1=1;
    let y1=r;
    while(y1 >= x1){

        points.add({x:x1, y:y1});
        points.add({x:-x1+1, y:y1});
        points.add({x:x1, y:-y1+1});
        points.add({x:-x1+1, y:-y1+1});
        points.add({x:y1, y:x1});
        points.add({x:y1, y:-x1+1});
        points.add({x:-y1+1, y:x1});
        points.add({x:-y1+1, y:-x1+1});

        let test1 = (r*2 - 1)*(r*2 - 1) < (x1+1)*(x1+1)*4 + y1*y1*4 && (x1+1)*(x1+1)*4 + y1*y1*4 < (r*2 + 1)*(r*2 + 1);
        let test2 = (r*2 - 1)*(r*2 - 1) < (y1-1)*(y1-1)*4 + x1*x1*4 && (y1-1)*(y1-1)*4 + x1*x1*4 < (r*2 + 1)*(r*2 + 1);

        if(test1){
            x1++;
        }
        else if(test2){
            y1--;
        }
        else{
            x1++;
            y1--;
        }

    }

    points.forEach(point => {
        point.x += x-1; //ms paint style offset where your stroke moves up and to the left a bit
        point.y += y-1;
    });

    return Array.from(points);
}

export function filledCircle(x:number, y:number, diameter:number) : Point[]{
    x = Math.trunc(x);
    y = Math.trunc(y);
    diameter = Math.trunc(diameter);

    let newPoints = new Set<Point>();
    let points = diameter%2==1 ? midpointCircle(x,y,diameter/2) : evenDiameterCircle(x,y,diameter/2);

    points.forEach(point => {
        let breaktime = false;
        let x0 = point.x;
        if(x0 > x){
            while(!breaktime){
                x0--;
                for(const po of points){ //why the hell doesnt js have a way to check structural equality
                    if(po.x === x0 && po.y === point.y) breaktime = true;
                }
                newPoints.add({x:x0,y:point.y});
            }
        }
        else if(x0 < x){
            while(!breaktime){
                x0++;
                for(const po of points){
                    if(po.x === x0 && po.y === point.y) breaktime = true;
                }
                newPoints.add({x:x0,y:point.y});
            }
        }
    });

    points.forEach(p => newPoints.add(p));
    return Array.from(newPoints);
}

export function thickCircle(x:number, y:number, diameter:number) : Point[]{
    x = Math.trunc(x);
    y = Math.trunc(y);
    diameter = Math.trunc(diameter);

    let newPoints = new Set<Point>();
    let points = diameter%2==1 ? midpointCircle(x,y,diameter/2) : evenDiameterCircle(x,y,diameter/2);

    points.forEach(point => {
        let x0 = point.x;
        if(x0 > x){
            x0--;
            newPoints.add({x:x0,y:point.y});
        }
        else if(x0 < x){
            x0++;
            newPoints.add({x:x0,y:point.y});

        }
    });
    points.forEach(p => newPoints.add(p));
    return Array.from(newPoints);
}


