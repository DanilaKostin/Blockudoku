package com.example.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class GameView extends SurfaceView {

    private SurfaceHolder holder;
    private GameThread gameThread;
    public int chose = 0, score = 0;
    public int Width , Height;
    public boolean loseMode = false;

    GameLogic[][] cells = new GameLogic[9][9];
    GameLogic[] buttons = new GameLogic[3];
    GameLogic[][] inButtons = new GameLogic[3][];
    Destinations[] inButtonsDest = new Destinations[3];
    GameLogic restartButton = new GameLogic();

    public GameView(Context context) {
        super(context);

        starter();
        generateNewBlocks();

        gameThread = new GameThread(this);

        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                gameThread.setRunning((true));
                gameThread.start();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                boolean retry = true;
                gameThread.setRunning(false);
                while (retry){
                    try {
                        gameThread.join();
                        retry = false;
                    } catch (InterruptedException e){
                    }
                }
            }
        });
    }

    protected void onDraw (Canvas canvas) {

        Width = this.getWidth();
        Height = this.getHeight();

        @SuppressLint("DrawAllocation") Paint cellsColor = new Paint();
        cellsColor.setColor(Color.GRAY);
        cellsColor.setStyle(Paint.Style.STROKE);
        cellsColor.setStrokeWidth(6);
        canvas.drawColor(Color.WHITE);

        @SuppressLint("DrawAllocation") Paint cellsColor2 = new Paint();
        cellsColor2.setColor(Color.LTGRAY);
        cellsColor2.setStyle(Paint.Style.FILL);
        cellsColor2.setStrokeWidth(6);

        @SuppressLint("DrawAllocation") Paint cellsColored = new Paint();
        cellsColored.setColor(Color.BLUE);
        cellsColored.setStyle(Paint.Style.FILL);
        cellsColored.setTextSize(72);

        @SuppressLint("DrawAllocation") Paint interfaceCells = new Paint();
        interfaceCells.setColor(Color.BLUE);
        interfaceCells.setStrokeWidth(12);
        interfaceCells.setStyle(Paint.Style.STROKE);

        @SuppressLint("DrawAllocation") Paint previewCells = new Paint();
        previewCells.setColor(Color.BLUE);
        previewCells.setStrokeWidth(3);
        previewCells.setStyle(Paint.Style.STROKE);

        @SuppressLint("DrawAllocation") Paint loseText = new Paint();
        loseText.setColor(Color.BLUE);
        loseText.setTextSize(60);

        if (loseMode) {
            canvas.drawRect(50, 500, 1030, 800, cellsColor);
            canvas.drawText("Вы проиграли!", 350, 600, loseText);
            canvas.drawText("Больше нет ходов.", 300, 700, loseText);
        } else {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (cells[i][j].colored == 1) {
                        canvas.drawRect(cells[i][j].left, cells[i][j].top, cells[i][j].right, cells[i][j].bottom, cellsColored);
                    } else {
                        if ((i < 3 || i >= 6) && (j < 3 || j >= 6) || (i >= 3 && i < 6) && (j >= 3 && j < 6)) {
                            canvas.drawRect(cells[i][j].left, cells[i][j].top, cells[i][j].right, cells[i][j].bottom, cellsColor2);
                            canvas.drawRect(cells[i][j].left, cells[i][j].top, cells[i][j].right, cells[i][j].bottom, cellsColor);
                        } else
                            canvas.drawRect(cells[i][j].left, cells[i][j].top, cells[i][j].right, cells[i][j].bottom, cellsColor);
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                if (buttons[i].colored == 0)
                    canvas.drawRect(buttons[i].left, buttons[i].top, buttons[i].right, buttons[i].bottom, cellsColor);
                else
                    canvas.drawRect(buttons[i].left, buttons[i].top, buttons[i].right, buttons[i].bottom, interfaceCells);
            }
            int w = (1075 - 50) / 3;
            for (int j = 0; j < 3; j++)
                for (int i = 0; i < inButtonsDest[j].size; i++) {
                    canvas.drawRect(inButtons[j][i].left, inButtons[j][i].top, inButtons[j][i].right, inButtons[j][i].bottom, previewCells);
                }
        }
        canvas.drawRect(restartButton.left, restartButton.top, restartButton.right, restartButton.bottom, interfaceCells);
        canvas.drawText("Счёт: " + score, 100, 125, cellsColored);
        canvas.drawText("Перезапуск", 600, 125, cellsColored);
    }

    float x1 = -100, y1 = -100;
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        if (inside((int)x1, (int)y1, cells[i][j])){
                            if (suitable(inButtonsDest[chose],i,j)){
                                int i1 = i, j1 = j;
                                for (int k = 0; k < inButtonsDest[chose].size; k++){
                                    if (k == 0){
                                        cells[i1][j1].colored = 1;
                                        continue;
                                    }
                                    if (inButtonsDest[chose].destinations[k] == 1){
                                        j1--;
                                        cells[i1][j1].colored = 1;
                                    }
                                    else {
                                        i1--;
                                        cells[i1][j1].colored = 1;
                                    }
                                }
                                checkKub();
                                generateNewBlocks();
                                if (loss())
                                    loseMode= true;
                            }
                        }
                        if(inside((int)x1, (int)y1, restartButton)){
                            score = 0;
                            clearBlocks();
                            generateNewBlocks();
                            loseMode = false;
                        }
                    }
                }
                for (int i = 0; i < 3; i++)
                if (inside((int)x1, (int)y1, buttons[i])){
                    for (int j = 0; j < 3; j++)
                        buttons[j].colored = 0;
                    buttons[i].colored = 1;
                    chose = i;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }
    public static class GameLogic {
        int id;
        int colored = 0;
        int left;
        int right;
        int top;
        int bottom;
    }

    public class Destinations {
        int size;
        int destinations[];
    }

    public boolean loss(){
        boolean flag = false;
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                for (int k = 0; k < 3; k++){
                    if (suitable(inButtonsDest[k], i, j)){
                        flag = true;
                    }
                }
            }
        }
        if (flag)
            return false;
        return true;
    }

    public void clearBlocks(){
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                cells[i][j].colored = 0;
            }
        }
    }
    public boolean suitable(Destinations dest, int x, int y){
        int index = 1;
        if (dest.size == 1 && cells[x][y].colored == 0)
            return true;
        if (cells[x][y].colored == 1)
            return false;
        while (index < dest.size){
            if (dest.destinations[index] == 1 ){
                y--;
            }
            else x--;
            if (y < 0 || x < 0 || cells[x][y].colored == 1)
                return false;
            index++;
        }
        return true;
    }
    public boolean inside(int x, int y, GameLogic cell){
        int rSign, lSign, tSign, bSign;
        rSign = cell.right-x;
        lSign = cell.left-x;
        tSign = cell.top-y;
        bSign = cell.bottom-y;
        if (rSign > 0 && lSign < 0 && tSign <0 && bSign > 0) {
            return true;
        }
        return false;
    }
    public void starter(){
        int w=(1075-50)/9;
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new GameLogic();
                cells[i][j].left = 30 + i * w;
                cells[i][j].right = (30 + (i + 1) * w);
                cells[i][j].top = 200 + j * w;
                cells[i][j].bottom =  (200 + (j + 1) * w);
                cells[i][j].id = (i+1)*10+j+1;
            }
        w=(1075-50)/3;
        for (int i = 0; i < 3; i++){
            buttons[i] = new GameLogic();
            buttons[i].left = 30 + i * w;
            buttons[i].right = (30 + (i + 1) * w);
            buttons[i].top = 1300;
            buttons[i].bottom =  1500;
            buttons[i].id = i;
        }
        restartButton.left = 590;
        restartButton.right =1010;
        restartButton.top = 50;
        restartButton.bottom = 150;
    }
    public void generateBlocks(int index){
            int max = 4;
            int min = 1;
            max -= min;
            inButtonsDest[index] = new Destinations();
            inButtonsDest[index].size =(int)(Math.random()*++max)+min;
            inButtonsDest[index].destinations = new int[inButtonsDest[index].size];
            for (int j = 0; j < inButtonsDest[index].size; j++){
                inButtonsDest[index].destinations[j] = generateDestination();
        }
    }

    public int generateDestination(){
        int max = 2;
        int min = 1;
        max -=min;
        return (int)(Math.random()*++max)+min;
    }
    public void block(int index){
        generateBlocks(index);
        inButtons[index] = new GameLogic[inButtonsDest[index].size];
        int w=(1075-50)/3;
        int scewX = 0;
        for (int i = 0; i < inButtonsDest[index].size; i++){
            inButtons[index][i] = new GameLogic();
            if (inButtonsDest[index].destinations[i] == 1 || i == 0){
                inButtons[index][i].left = w - 25 +w*index-scewX;
                inButtons[index][i].right = w - 50 - 25 +w*index-scewX;
                inButtons[index][i].top = 1450-50*i+scewX;
                inButtons[index][i].bottom = 1500-i*50+scewX;
            }
            else {
                scewX+=50;
                inButtons[index][i].left = w - 25 +w*index-scewX;
                inButtons[index][i].right = w - 50 - 25 +w*index-scewX;
                inButtons[index][i].top = 1450-50*(i)+scewX;
                inButtons[index][i].bottom = 1500-(i)*50+scewX;
            }
        }
    }
    public void generateNewBlocks(){
        block(0);
        block(1);
        block(2);
    }

    public void checkKub(){
        for (int i = 0; i < 3; i++){
            for (int j =0; j < 3;j++){
                if (cells[3*i][3*j].colored == 1 && cells[3*i][3*j+1].colored == 1 && cells[3*i][3*j+2].colored == 1 &&
                        cells[3*i+1][3*j].colored == 1 && cells[3*i+1][3*j+1].colored == 1 && cells[3*i+1][3*j+2].colored == 1 &&
                        cells[3*i+2][3*j].colored == 1 && cells[3*i+2][3*j+1].colored == 1 && cells[3*i+2][3*j+2].colored == 1)
                {
                    cells[3*i][3*j].colored = 0;
                    cells[3*i][3*j+1].colored = 0;
                    cells[3*i][3*j+2].colored = 0;
                    cells[3*i+1][3*j].colored = 0;
                    cells[3*i+1][3*j+1].colored = 0;
                    cells[3*i+1][3*j+2].colored = 0;
                    cells[3*i+2][3*j].colored = 0;
                    cells[3*i+2][3*j+1].colored = 0;
                    cells[3*i+2][3*j+2].colored = 0;
                    score+=9;
                }
            }
        }
    }
}