package com.gorisse.thomas.realitycore.filament

import com.google.android.filament.Box
import com.gorisse.thomas.realitycore.component.Position
import com.gorisse.thomas.realitycore.component.Size

/**
 * ### A representation of a shape.
 */
open class ShapeResource(var center: Position, var halfExtent: Size) {

    constructor(box: Box) : this(
        Position(box.center[0], box.center[1], box.center[2]),
        Size(box.halfExtent[0], box.halfExtent[1], box.halfExtent[2])
    )
}