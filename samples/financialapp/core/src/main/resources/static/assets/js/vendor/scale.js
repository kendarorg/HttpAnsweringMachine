/*
 * JQuery LineChart
 *
 * Released under the MIT License
 * --------------------------------------------------------------------------------
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Stepkin Kirill (https://github.com/kirillstepkin)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * --------------------------------------------------------------------------------
 */

$.fn.linechart = function(params) {
    var chart = this,
        marks = params.data,
        SCALE_WIDTH = params.width || 50,
        SCALE_HEIGHT = params.height || 50,
        BLOCK_WIDTH = params.boxSize || 20,
        hasLine = params.line || false,
        theme = params.theme || 'default';

    renderRulers();

    function renderRulers() {
        var rulerCont = chart[0];
        rulerCont.className = "ruler-container " + theme;

        var chartFragment = document.createDocumentFragment();

        marks.forEach(function(mark, markNum) {
            var rulerItem = document.createElement('div');
            rulerItem.className = "ruler-row";
            renderRow(rulerItem, mark, markNum);
            chartFragment.appendChild(rulerItem);
        });

        rulerCont.appendChild(chartFragment);
    }

    function renderRow(rulerItem, mark, markNum) {
        var style = {
            width: BLOCK_WIDTH + 'px',
            height: BLOCK_WIDTH + 'px'
        };

        for (var i=0; i < SCALE_HEIGHT; i++) {
            var markItem = document.createElement('div');
            markItem.setAttribute("data-type", mark.value - 1 == i ? mark.value : i + 1);
            markItem.setAttribute("style", 'width: ' + style.width + '; height: ' + style.height + ';');

            if (SCALE_HEIGHT - mark.value > i) markItem.className = "mark empty";
            if (SCALE_HEIGHT - mark.value == i) markItem.className = "mark";
            if (SCALE_HEIGHT - mark.value < i) markItem.className = "mark painted";

            if (SCALE_HEIGHT - mark.value == i && markNum < marks.length - 1 && hasLine) {
                var line = document.createElement('div');
                line.className = "line";
                var opts = calculateLineOptions(mark.value, marks[markNum+1].value);
                line.setAttribute("style", 'width: ' + opts.width + '; transform: ' + opts.transform + '; top: ' + opts.top + '; left: ' + opts.left + ';');
                markItem.appendChild(line);

                var tooltip = document.createElement('div');
                tooltip.className = "tooltiptext";
                tooltip.innerHTML = "<div>" + mark.value + "</div>" + "<div>" + mark.title + "</div>";
                markItem.appendChild(tooltip);
            }

            rulerItem.appendChild(markItem);
        }
    }

    function calculateLineOptions(currentMark, nextMark) {
        console.log(currentMark, nextMark)
        var AC = BLOCK_WIDTH,
            BC = Math.abs(nextMark - currentMark) * BLOCK_WIDTH;
        var AB = Math.hypot( AC, BC );
        var angleA = Math.fround( Math.asin( BC / AB ) * 180 / Math.PI);

        if (nextMark > currentMark) angleA = -angleA;

        return {
            width: AB + 'px',
            transform: 'rotate(' + angleA + 'deg)',
            top: parseInt(BLOCK_WIDTH/2) + 'px',
            left: parseInt(BLOCK_WIDTH/2) + 'px'
        };
    }
};