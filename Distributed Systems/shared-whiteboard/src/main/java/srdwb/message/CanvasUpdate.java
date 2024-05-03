package srdwb.message;

import srdwb.Shapes.Shape;

@JsonSerializable
public class CanvasUpdate extends Message {

    @JsonElement
    public Shape shape;

    public CanvasUpdate() {

    }

    public CanvasUpdate(Shape shape) {
        this.shape = shape;
    }


}
