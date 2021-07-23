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

package com.hammergenics.core.stages.ui.auxiliary.types;

import com.badlogic.gdx.utils.Array;
import com.hammergenics.core.stages.ui.auxiliary.HGTreeVisTableNode;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTree;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class ArrayAsTreeVisTable extends VisTable {
    public Array<?> array;
    public VisLabel titleL;
    public VisTable valueT;

    public VisTree<HGTreeVisTableNode, VisLabel> arrayTree;
    public VisScrollPane arrayTreeScrollPane;

    public ArrayAsTreeVisTable(Array<?> array, boolean title, VisLabel titleL) {
        if (array != null) { this.array = array; }
        if (titleL != null) { this.titleL = titleL; } else { this.titleL = new VisLabel("Array: "); }

        arrayTree = new VisTree<>();
        arrayTreeScrollPane = new VisScrollPane(arrayTree);

        valueT = new VisTable();
        valueT.add(arrayTreeScrollPane);

        if (title) { add(this.titleL).padRight(5f).right(); };
        add(valueT).expandX().fillX();
        row();
    }

    public void setArray(Iterable<?> array) {
        arrayTree.clearChildren();
        if (array == null) { return; }
        for (Object obj: array) {
            arrayTree.add(new HGTreeVisTableNode(new HGTreeVisTableNode.HGTreeVisTable(obj.toString())));
        }
    }
}