/*******************************************************************************
 * Copyright 2021 Nail Sharipov (sharipovn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.hammergenics.core.stages.ui.auxiliary;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGTreeVisTableNode extends Tree.Node<HGTreeVisTableNode, Integer, HGTreeVisTableNode.HGTreeVisTable> {
    public HGTreeVisTableNode(HGTreeVisTable actor) { super(actor); }

    public static class HGTreeVisTable extends VisTable {
        public Cell<?> cell1, cell2, cell3, cell4;
        public VisLabel label;
        public FileHandle fileHandle; // TODO: this should be a generic value: T value instead of FileHandle fileHandle

        public HGTreeVisTable(CharSequence text) { this(text, Color.WHITE, null); }
        public HGTreeVisTable(CharSequence text, FileHandle fileHandle) { this(text, Color.WHITE, fileHandle); }
        public HGTreeVisTable(CharSequence text, Color textColor) { this(text, textColor, null); }

        public HGTreeVisTable(CharSequence text, Color textColor, FileHandle fileHandle) {
            this.label = new VisLabel(text, textColor);
            this.fileHandle = fileHandle;

            cell1 = add().padRight(2f); cell2 = add().padRight(2f);
            cell3 = add().padRight(2f); cell4 = add().padRight(2f);
            add(this.label);
        }

        public FileHandle getFileHandle() { return fileHandle; }
        public HGTreeVisTable setCell1(Actor actor) { this.cell1.setActor(actor); return this; }
        public HGTreeVisTable setCell2(Actor actor) { this.cell2.setActor(actor); return this; }
        public HGTreeVisTable setCell3(Actor actor) { this.cell3.setActor(actor); return this; }
        public HGTreeVisTable setCell4(Actor actor) { this.cell4.setActor(actor); return this; }
    }
}