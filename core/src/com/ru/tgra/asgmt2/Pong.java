package com.ru.tgra.asgmt2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;

import java.io.Console;
import java.nio.FloatBuffer;

import com.badlogic.gdx.utils.BufferUtils;

public class Pong extends ApplicationAdapter {
	
	private FloatBuffer vertexBuffer;

	private FloatBuffer modelMatrix;
	private FloatBuffer projectionMatrix;

	private int renderingProgramID;
	private int vertexShaderID;
	private int fragmentShaderID;

	private int positionLoc;

	private int modelMatrixLoc;
	private int projectionMatrixLoc;

	private int colorLoc;
	
	private float paddleSize;
	private float paddleSpeed;
	
	private float normalPaddleX;
	private float normalPaddleY;
	private float normalEdgeX;
	private float normalEdgeY;
	
	private float paddle1PositionX;
	private float paddle1PositionY;
	
	private float paddle2PositionX;
	private float paddle2PositionY;
	
	private float ballVectorX;
	private float ballVectorY;
	private float ballPositionX;
	private float ballPositionY;
	
	private float paddleArray[];
	
	private float blocksArray[][];

	@Override
	public void create () {

		String vertexShaderString;
		String fragmentShaderString;

		vertexShaderString = Gdx.files.internal("shaders/simple2D.vert").readString();
		fragmentShaderString =  Gdx.files.internal("shaders/simple2D.frag").readString();

		vertexShaderID = Gdx.gl.glCreateShader(GL20.GL_VERTEX_SHADER);
		fragmentShaderID = Gdx.gl.glCreateShader(GL20.GL_FRAGMENT_SHADER);
	
		Gdx.gl.glShaderSource(vertexShaderID, vertexShaderString);
		Gdx.gl.glShaderSource(fragmentShaderID, fragmentShaderString);
	
		Gdx.gl.glCompileShader(vertexShaderID);
		Gdx.gl.glCompileShader(fragmentShaderID);

		renderingProgramID = Gdx.gl.glCreateProgram();
	
		Gdx.gl.glAttachShader(renderingProgramID, vertexShaderID);
		Gdx.gl.glAttachShader(renderingProgramID, fragmentShaderID);
	
		Gdx.gl.glLinkProgram(renderingProgramID);

		positionLoc				= Gdx.gl.glGetAttribLocation(renderingProgramID, "a_position");
		Gdx.gl.glEnableVertexAttribArray(positionLoc);

		modelMatrixLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_modelMatrix");
		projectionMatrixLoc	= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_projectionMatrix");

		colorLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_color");

		Gdx.gl.glUseProgram(renderingProgramID);

		float[] pm = new float[16];

		pm[0] = 2.0f / Gdx.graphics.getWidth(); pm[4] = 0.0f; pm[8] = 0.0f; pm[12] = -1.0f;
		pm[1] = 0.0f; pm[5] = 2.0f / Gdx.graphics.getHeight(); pm[9] = 0.0f; pm[13] = -1.0f;
		pm[2] = 0.0f; pm[6] = 0.0f; pm[10] = 1.0f; pm[14] = 0.0f;
		pm[3] = 0.0f; pm[7] = 0.0f; pm[11] = 0.0f; pm[15] = 1.0f;

		projectionMatrix = BufferUtils.newFloatBuffer(16);
		projectionMatrix.put(pm);
		projectionMatrix.rewind();
		Gdx.gl.glUniformMatrix4fv(projectionMatrixLoc, 1, false, projectionMatrix);


		float[] mm = new float[16];

		mm[0] = 1.0f; mm[4] = 0.0f; mm[8] = 0.0f; mm[12] = 0.0f;
		mm[1] = 0.0f; mm[5] = 1.0f; mm[9] = 0.0f; mm[13] = 0.0f;
		mm[2] = 0.0f; mm[6] = 0.0f; mm[10] = 1.0f; mm[14] = 0.0f;
		mm[3] = 0.0f; mm[7] = 0.0f; mm[11] = 0.0f; mm[15] = 1.0f;

		modelMatrix = BufferUtils.newFloatBuffer(16);
		modelMatrix.put(mm);
		modelMatrix.rewind();

		Gdx.gl.glUniformMatrix4fv(modelMatrixLoc, 1, false, modelMatrix);

		//COLOR IS SET HERE
		Gdx.gl.glUniform4f(colorLoc, 0.7f, 0.2f, 0, 1);

		paddleSize = 100.0f;
		paddleSpeed = 4.0f;
		
		normalPaddleX = 1;
		normalPaddleY = 0;
		
		normalEdgeX = 0;
		normalEdgeY = 1;

		//VERTEX ARRAY IS FILLED HERE
		float[] array = {-50.0f, -50.0f,
						-50.0f, 50.0f,
						50.0f, -50.0f,
						50.0f, 50.0f};

		vertexBuffer = BufferUtils.newFloatBuffer(8);
		vertexBuffer.put(array);
		vertexBuffer.rewind();
		
		paddle1PositionX = 10.0f;
		paddle1PositionY = 200.0f;
		
		paddle2PositionX = Gdx.graphics.getWidth() - 10;
		paddle2PositionY = 200.0f;
		
		ballVectorX = 0;
		ballVectorY = 0;
		ballPositionX = 200.0f;
		ballPositionY = 200.0f;
		
		float[] tmp = {-35.0f, -100.0f,
					   -35.0f,  100.0f,
						35.0f, -100.0f,
						35.0f,  100.0f};
		paddleArray = tmp;
		
		int row = 9;
		int col = 20;
		
		blocksArray = new float[row][col];
		for(int i = 0; i < blocksArray.length; i++) {
			for(int j = 5; j+5 < blocksArray[0].length; j++) {
				blocksArray[i][j] = 1;
			}
		}
		
	}
	
	private float getTHit(float BX, float BY, float normalX, float normalY, float ballX, float ballY) {
		return (normalX*(BX-ballPositionX)+normalY*(BY-ballPositionY))/(normalX*ballVectorX + normalY*ballVectorY);
	}
	
	private float getPHitX(float THit) {
		return ballPositionX + ballVectorX*THit;
	}
	
	private float getPHitY(float THit) {
		return ballPositionY + ballVectorY*THit;
	}
	
	private void getNewVector(float normalPaddleX, float normalPaddleY) {
		float denominator = (float) Math.sqrt(normalPaddleX*normalPaddleX + normalPaddleY*normalPaddleY);
		float a = 2*(ballVectorX*(normalPaddleX/denominator)+ballVectorY*(normalPaddleY/denominator));
		//X axis
		ballVectorX = 1.0f*(ballVectorX - (a*(normalPaddleX/denominator)));
		//Y axis
		ballVectorY = 1.0f*(ballVectorY - (a*(normalPaddleY/denominator)));
		
		if(ballVectorX < 0) {
			normalPaddleX = 1;
		}else {
			normalPaddleX = -1;
		}
		if(ballVectorY < 0) {
			normalPaddleX = -1;
		}else {
			normalPaddleX = 1;
		}
	}
	
	private void getNewPaddleVector(float paddleY) {
		//Can be a constant but have it here if i want the game to speed up
		float totalVectorLenght = 3;//(float) Math.sqrt(ballVectorY*ballVectorY+ballVectorX*ballVectorX);

		if(ballPositionY-paddleY > 0) {
			ballVectorY = ((ballPositionY-paddleY)/100)*4;
		} else {
			ballVectorY = ((ballPositionY-paddleY)/100)*4;
		}		
		if(ballVectorX > 0) {
			ballVectorX = (float) -Math.sqrt(totalVectorLenght*totalVectorLenght-ballVectorY*ballVectorY);
		} else {
			ballVectorX = (float) Math.sqrt(totalVectorLenght*totalVectorLenght-ballVectorY*ballVectorY);
		}
	}
	
	private void paddleReflection(float paddleX, float paddleY, int arrayX, int arrayY, float ballX, float ballY) {
		float THit = getTHit(paddleX + paddleArray[arrayX], paddleY + paddleArray[arrayY],normalPaddleX, normalPaddleY, ballX, ballY);
		if(1 > THit) {
			if( ( ballPositionY < (paddleY + paddleArray[3]) ) && ( ballPositionY > (paddleY + paddleArray[arrayY]) ) ) {
				getNewPaddleVector(paddleY);
				ballPositionX = ballVectorX * THit + getPHitX(THit);
				ballPositionY = ballVectorY * THit + getPHitY(THit);
			}
		}
	}
	
	private void edgeReflection(float BX, float BY, boolean top, float ballX, float ballY) {
		float THit = getTHit(BX, BY, normalEdgeX, normalEdgeY, ballX, ballY);
		if(1 > THit) {
			getNewVector(normalEdgeX, normalEdgeY);
			ballPositionX = ballVectorX * THit + getPHitX(THit);
			ballPositionY = ballVectorY * THit + getPHitY(THit);
		}
	}
	
	private void blockReflection() {
		for(int i = 0; i < blocksArray.length; i++) {
			for(int j = 0; j < blocksArray[0].length; j++) {	
				if(blocksArray[i][j] == 1) {
					float blockWidth = 100*0.3f;
					float blockHeight = 100*0.6f;
					if(ballVectorX > 0) {
						if(ballVectorY > 0) {
							//Going up to the right
							
							//check top right
							float topRight = getTHit((float)(i*80)+60+(blockWidth/2), (float)(j*40)+132+(blockHeight/2), -1.0f, 0.0f, 50, 50);
							//check top left
							float topLeft = getTHit((float)(i*80)+60-(blockWidth/2), (float)(j*40)+132+(blockHeight/2), -1.0f, 0.0f, 50, 50);
							//check bottom right
							float bottomRight = getTHit((float)(i*80)+60+(blockWidth/2), (float)(j*40)+132-(blockHeight/2), -1.0f, 0.0f, 50, 50);
							
							if(topRight < topLeft) {
								if(topRight < bottomRight) {
									//topRight
									if(topRight < 1 && topRight < 0) {
										System.out.println("TopRight");
										if( ballPositionY+50 < i*80+60+blockHeight/2 && ballPositionY+50 > i*80+60-blockHeight/2) {
											if((float) Math.sqrt(ballVectorY*ballVectorY+ballVectorX*ballVectorX) > 0) {
												blocksArray[i][j] = 0;
												getNewVector(-1.0f,0.0f);
												//ballVectorX = -ballVectorX;
											}
										}
									}	
								}else {
									//bottomRight	
									if(bottomRight < 1 && bottomRight < 0) {
										System.out.println("BottomRight");
										if( ballPositionY-50 < i*80+60+blockHeight/2 && ballPositionY-50 > i*80+60-blockHeight/2) {
											if((float) Math.sqrt(ballVectorY*ballVectorY+ballVectorX*ballVectorX) > 0) {
												blocksArray[i][j] = 0;
												getNewVector(-1.0f,0.0f);
												//ballVectorX = -ballVectorX;
											}
										}
									}	
								}
							}else{
								if(topLeft < bottomRight) {
									//topLeft	
									if(topLeft < 1 && topLeft < 0) {
										System.out.println("TopLeft");
										if( ballPositionX+50 < j*40-132+blockHeight/2 && ballPositionX+50 > j*40-132-blockHeight/2) {
											if((float) Math.sqrt(ballVectorY*ballVectorY+ballVectorX*ballVectorX) > 0) {
												blocksArray[i][j] = 0;
												getNewVector(-1.0f,0.0f);
												//ballVectorX = -ballVectorX;
											}
										}	
									}	
								} else {
									//bottomRight
									if(bottomRight < 1 && bottomRight < 0) {
										float temp = ballPositionY-50;
										float temp1 = i*80+60+blockHeight/2;
										float temp2 = i*80+60-blockHeight/2;
										System.out.println("BottomRight2");
										System.out.println("ballPositionY: " + temp + "  " + temp1 + "," + temp2 );
										if( ballPositionY-50 < i*80+60+blockHeight/2 && ballPositionY-50 > i*80+60-blockHeight/2) {
											if((float) Math.sqrt(ballVectorY*ballVectorY+ballVectorX*ballVectorX) > 0) {
												blocksArray[i][j] = 0;
												getNewVector(-1.0f,0.0f);
												//ballVectorX = -ballVectorX;
											}
										}
									}	
								}
							}
							
							/*
							if(getTHit((float)(j*40)+132-(blockHeight/2), (float)(i*80)+60-(blockWidth/2), -1.0f, 0.0f, 50, 50) < 1 && getTHit((float)(j*40)+132-(blockHeight/2), (float)i*80+60-(blockWidth/2), -1.0f, 0.0f, 50, 50) > 0) {
								if( ballPositionY-50 < i*80+60+blockHeight/2 && ballPositionY-50 > i*80+60-blockHeight) {
									if((float) Math.sqrt(ballVectorY*ballVectorY+ballVectorX*ballVectorX) > 0) {
										blocksArray[i][j] = 0;
										getNewVector(-10.f,0.0f);
										//ballVectorX = -ballVectorX;
									}
								}
							}
							*/
							
							
						}else {
							/*if(getTHit((float)(j*40)+132-(blockHeight/2), (float)(i*80)+60-(blockWidth/2), 1.0f, 0.0f, 50, 50) < 1 && getTHit((float)(j*40)+132-(blockHeight/2), (float)i*80+60-(blockWidth/2), 1.0f, 0.0f, 50, 50) > 0) {
								if( ballPositionY-50 < i*80+60+blockHeight/2 && ballPositionY-50 > i*80+60-blockHeight) {
									if((float) Math.sqrt(ballVectorY*ballVectorY+ballVectorX*ballVectorX) > 0) {
										blocksArray[i][j] = 0;
										getNewVector(-10.f,0.0f);
										//ballVectorX = -ballVectorX;
									}
								}
							}
							*/
						}
					} else {
						/*
						if(ballVectorY > 0) {
							//Niðri vinsta megin
							//left = (i*80)+60-(blockWidth/2);
							//right = (i*80)+60+(blockWidth/2);
							//up = (j*40)+132+(blockHeight/2);
							//down = (j*40)-132-(blockHeight/2);
							
							if(getTHit((float)(j*40)+132+(blockHeight/2), (float)(i*80)+60, -1.0f, 0.0f, -50, -50) < 1 && getTHit((float)(j*40)+132+(blockHeight/2), (float)i*80+60, -1.0f, 0.0f, -50, -50) > 0) {
								if( ballPositionY-50 < i*80+60+blockHeight/2 && ballPositionY-50 > i*80+60-blockHeight) {
									if((float) Math.sqrt(ballVectorY*ballVectorY+ballVectorX*ballVectorX) > 0) {
										blocksArray[i][j] = 0;
										ballVectorX = -ballVectorX;
									}
								}
							}	
							
						}else {
							if(getTHit((float)(j*40)+132+(blockHeight/2), (float)(i*80)+60, 1.0f, 0.0f, -50, -50) < 1 && getTHit((float)(j*40)+132+(blockHeight/2), (float)i*80+60, 1.0f, 0.0f, -50, -50) > 0) {
								if( ballPositionY-50 < i*80+60+blockHeight/2 && ballPositionY-50 > i*80+60-blockHeight) {
									if((float) Math.sqrt(ballVectorY*ballVectorY+ballVectorX*ballVectorX) > 0) {
										blocksArray[i][j] = 0;
										ballVectorX = -ballVectorX;
									}
								}
							}
						}
						*/
					}
				}
			}
		}
	}

	
	private void update()
	{
		if(Gdx.input.justTouched())
		{ 

		}
		
		if (ballVectorX < 0) {
			paddleReflection(paddle1PositionX, paddle1PositionY, 4, 5, -50, -50);
			blockReflection();
		} else {
			paddleReflection(paddle2PositionX, paddle2PositionY, 0, 1, 50, 50);
			blockReflection();
		}
		if (ballVectorY < 0) {
			edgeReflection(0, 5, false, -50, -50);
		} else {
			edgeReflection(0, Gdx.graphics.getHeight()-5, true, 50, 50);
			
		}
		if (ballPositionX <= 0) {
			ballPositionX = 200.0f;
			ballPositionY = 200.0f;
			ballVectorX = 0;
			ballVectorY = 0;
		}else if (ballPositionX >= Gdx.graphics.getWidth()) {
			ballPositionX = 200.0f;
			ballPositionY = 200.0f;
			ballVectorX = 0;
			ballVectorY = 0;
		}
		
		ballPositionX += ballVectorX; 
		ballPositionY += ballVectorY;
		
		//Paddle1 Movement
		if(Gdx.input.isKeyPressed(Input.Keys.W)) {
			if (paddle1PositionY+paddleSize < Gdx.graphics.getHeight()) {
				paddle1PositionY += paddleSpeed;
			} 
		}
		if(Gdx.input.isKeyPressed(Input.Keys.S)) {
			if (paddle1PositionY-paddleSize > 0) {
				paddle1PositionY -= paddleSpeed;
			}
		}
		if(Gdx.input.isKeyPressed(Input.Keys.D)) {
			if (ballVectorX == 0 && 0 == ballVectorY) {
				ballVectorX = -3;
				ballVectorY = 3;
			}
		}
		if(Gdx.input.isKeyPressed(Input.Keys.A)) {
			if (ballVectorX == 0 && 0 == ballVectorY) {
				ballVectorX = -3;
				ballVectorY = -3;
			}
		}

		//Paddle2 Movement
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
			if (paddle2PositionY+paddleSize < Gdx.graphics.getHeight()) {
				paddle2PositionY += paddleSpeed;
			}
		}
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			if (paddle2PositionY-paddleSize > 0) {
				paddle2PositionY -= paddleSpeed;
			}
		}
		
		//do all updates to the game
	}
	
	private void drawBackround() {
		Gdx.gl.glClearColor(0.0f, 0.0f, 0, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	private void drawPaddle(float x, float y) {
		clearModelMatrix();
		setModelMatrixScale(0.5f, 2.0f);
		setModelMatrixTranslation(x, y);	
		Gdx.gl.glUniform4f(colorLoc, 1.0f, 1.0f, 1, 1);
		Gdx.gl.glVertexAttribPointer(positionLoc, 2, GL20.GL_FLOAT, false, 0, vertexBuffer);
		Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_STRIP, 0, 4);
	}
	private void drawBall(float x, float y) {
		clearModelMatrix();
		setModelMatrixScale(0.2f, 0.2f);
		setModelMatrixTranslation(x, y);
		Gdx.gl.glVertexAttribPointer(positionLoc, 2, GL20.GL_FLOAT, false, 0, vertexBuffer);
		Gdx.gl.glUniform4f(colorLoc, 1.0f, 1.0f, 1, 1);
		Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	private void drawMiddle(float x, float y) {
		clearModelMatrix();
		setModelMatrixScale(0.2f, 0.5f);
		setModelMatrixTranslation(x, y);
		Gdx.gl.glVertexAttribPointer(positionLoc, 2, GL20.GL_FLOAT, false, 0, vertexBuffer);
		Gdx.gl.glUniform4f(colorLoc, 0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	private void drawBlock(float x, float y) {
		clearModelMatrix();
		setModelMatrixScale(0.3f, 0.6f);
		setModelMatrixTranslation(x, y);
		Gdx.gl.glVertexAttribPointer(positionLoc, 2, GL20.GL_FLOAT, false, 0, vertexBuffer);
		Gdx.gl.glUniform4f(colorLoc, 0.5f, 0.5f, 0.2f, 1);
		Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	private void drawBlocks(int level) {
		for(int i = 0; i < blocksArray.length; i++) {
			for(int j = 0; j < blocksArray[0].length; j++) {	
				if (blocksArray[i][j] == 1) {
					drawBlock((j*40)+132, (i*80) +60);	
				}
			}
		}
	}

	private void display()
	{
		//do all actual drawing and rendering here
		drawBackround();
		
		for(int i = 0; i < Gdx.graphics.getHeight(); i+=Gdx.graphics.getHeight()/9) {
			drawMiddle(Gdx.graphics.getWidth()/2, i);
		}
		
		drawBlocks(1);
		
		drawPaddle (paddle1PositionX, paddle1PositionY);
		drawPaddle (paddle2PositionX, paddle2PositionY);
		
		drawBall(ballPositionX, ballPositionY);
		
		
	}

	@Override
	public void render () {
		
		//put the code inside the update and display methods, depending on the nature of the code
		update();
		display();

	}

	private void clearModelMatrix()
	{
		modelMatrix.put(0, 1.0f);
		modelMatrix.put(1, 0.0f);
		modelMatrix.put(2, 0.0f);
		modelMatrix.put(3, 0.0f);
		modelMatrix.put(4, 0.0f);
		modelMatrix.put(5, 1.0f);
		modelMatrix.put(6, 0.0f);
		modelMatrix.put(7, 0.0f);
		modelMatrix.put(8, 0.0f);
		modelMatrix.put(9, 0.0f);
		modelMatrix.put(10, 1.0f);
		modelMatrix.put(11, 0.0f);
		modelMatrix.put(12, 0.0f);
		modelMatrix.put(13, 0.0f);
		modelMatrix.put(14, 0.0f);
		modelMatrix.put(15, 1.0f);

		Gdx.gl.glUniformMatrix4fv(modelMatrixLoc, 1, false, modelMatrix);
	}
	private void setModelMatrixTranslation(float xTranslate, float yTranslate)
	{
		modelMatrix.put(12, xTranslate);
		modelMatrix.put(13, yTranslate);

		Gdx.gl.glUniformMatrix4fv(modelMatrixLoc, 1, false, modelMatrix);
	}
	private void setModelMatrixScale(float xScale, float yScale)
	{
		modelMatrix.put(0, xScale);
		modelMatrix.put(5, yScale);

		Gdx.gl.glUniformMatrix4fv(modelMatrixLoc, 1, false, modelMatrix);
	}
}