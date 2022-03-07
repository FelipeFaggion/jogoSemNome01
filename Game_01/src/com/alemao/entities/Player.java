package com.alemao.entities;


import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.alemao.graficos.Spritesheet;
import com.alemao.main.Game;
import com.alemao.world.Camera;
import com.alemao.world.World;

public class Player extends Entity{

	public boolean right,up,left,down;
	public int right_dir = 0, left_dir = 1;
	public int up_dir = 0, down_dir = 0;
	public int dir = right_dir;
	public double speed = 1.5;
	
	private int frames = 0, maxFrames = 5, index = 0, maxIndex = 3;
	private boolean moved = false;
	
	private BufferedImage[] rightPlayer;
	private BufferedImage[] leftPlayer;
	
	private BufferedImage[] rightBobba;
	private BufferedImage[] leftBobba;
	private BufferedImage[] upBobba;
	private BufferedImage[] downBobba;
	
	private BufferedImage playerDamage;
	private BufferedImage playerDamage2;
	
	private boolean hasGun = false;
	public boolean shoot = false;
	public boolean mouseShoot = false;
	public static boolean playMusic = false;
	
	public int mx,my;
	
	public int ammo = 0;
	
	public boolean isDamaged = false;
	private int damageFrames = 0;
	
	public double life = 100, maxlife = 100;
	
	public Player(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
		
		rightPlayer = new BufferedImage[4];
		leftPlayer = new BufferedImage[4];
		playerDamage = Game.spritesheet.getSprite(96, 0, 16, 16);
		playerDamage2 = Game.spritesheet.getSprite(128, 0, 16, 16);
		
		rightBobba = new BufferedImage[4];
		leftBobba = new BufferedImage[4];
		upBobba = new BufferedImage[4];
		downBobba = new BufferedImage[4];
		
		for(int i = 0; i <4; i++) {
			rightPlayer[i] = Game.spritesheet.getSprite((i*16), 0, 16, 16);
			leftPlayer[i] = Game.spritesheet.getSprite((i*16), 16, 16, 16);
			
			rightBobba[i] = Game.spritesheet.getSprite((i*16), 48, 16, 16);
			leftBobba[i] = Game.spritesheet.getSprite((i*16), 64, 16, 16);
			upBobba[i] = Game.spritesheet.getSprite((i*16), 80, 16, 16);
			downBobba[i] = Game.spritesheet.getSprite((i*16), 96, 16, 16);
		}
		
		
	}
	
	public void tick() {
		moved = false;
		if(right && World.isFree((int)(x+speed),this.getY())) {
			moved = true;
			dir = right_dir;
			x+=speed;
		}else if(left && World.isFree((int)(x-speed),this.getY())) {
			moved = true;
			dir = left_dir;
			x-=speed;
		}
		if(up && World.isFree(this.getX(),(int)(y-speed))){
			moved = true;
			y-=speed;
		}else if(down && World.isFree(this.getX(),(int)(y+speed))) {
			moved = true;
			y+=speed;
		}
		
		if(moved) {
			frames++;
			if(frames == maxFrames) {
				frames = 0;
				index++;
				if(index > maxIndex) {
					index = 0;
				}
			}
		}
		
		checkCollisionLifePack();
		checkCollisinAmmo();
		checkCollisinGun();
		
		if(isDamaged) {
			this.damageFrames++;
			if(this.damageFrames == 8) {
				this.damageFrames = 0;
				isDamaged = false;
			}
		}
		
		
		if(shoot) {
			shoot = false;
			if(hasGun && ammo > 0) {
				ammo--;
				int dx = 0;
				int px = 0;
				int py = 8;
				//System.out.println("atirando");
				if(dir == right_dir) {
					px = 18;
					dx = 1;
				} else {
					px = -8;
					dx = -1;
				}
				
				BulletShoot bullet = new BulletShoot(this.getX()+px,this.getY()+py, 3, 3, null, dx, 0);
				Game.bullets.add(bullet);
			}
		}
		
		
		/*if(mouseShoot) {
			//System.out.println("Atirou com o mouse");
			mouseShoot = false;
			
			if(hasGun && ammo > 0) {
				ammo--;
				int px = 0, py = 8;
				double angle = 0;
				if(dir == right_dir) {
					px = 18;
					angle = Math.atan2(my - (this.getY()+py - Camera.y), mx - (this.getX()+px - Camera.x));
				} else {
					px = -8;
					angle = Math.atan2(my - (this.getY()+py - Camera.y), mx - (this.getX()+px - Camera.x));
				}
				
				double dx = Math.cos(angle);
				double dy = Math.sin(angle);
				
				BulletShoot bullet = new BulletShoot(this.getX()+px,this.getY()+py, 3, 3, null, dx, dy);
				Game.bullets.add(bullet);
			}
		}*/
		
		if(life <= 0) {
			life = 0;
			Game.gameState = "GAME_OVER";
		}
		
		Camera.x = Camera.clamp(this.getX() - (Game.WIDTH/2), 0, World.WIDTH*16 - Game.WIDTH);
		Camera.y = Camera.clamp(this.getY() - (Game.HEIGHT/2), 0, World.HEIGHT*16 - Game.HEIGHT);
	}
	
	
	public void checkCollisinGun() {
		for(int i = 0; i < Game.entities.size(); i++) {
			Entity atual = Game.entities.get(i);
			if(atual instanceof Weapon) {
				if(Entity.isColidding(this, atual)) {
					hasGun = true;
					//System.out.println("Pegou arma");
					Game.entities.remove(atual);
				}
			}
		}
	}
	
	public void checkCollisinAmmo() {
		for(int i = 0; i < Game.entities.size(); i++) {
			Entity atual = Game.entities.get(i);
			if(atual instanceof Bullet) {
				if(Entity.isColidding(this, atual)) {
					ammo+=100;
					//System.out.println("Municao atual"+ ammo);
					Game.entities.remove(atual);
				}
			}
		}
	}
	
	public void checkCollisionLifePack() {
		if(this.life < 100) {
			for(int i = 0; i < Game.entities.size(); i++) {
				Entity atual = Game.entities.get(i);
				if(atual instanceof Lifepack) {
					if(Entity.isColidding(this, atual)) {
						life+=10;
						if(life > 100) {
							life = 100;
						}
						Game.entities.remove(atual);
					}
				}
			}
		}
	}
	
	
	public void render(Graphics g) {
		if(!isDamaged) {
			if(dir == right_dir) {
				g.drawImage(rightPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y, null);
				if(hasGun) {
					g.drawImage(Entity.GUN_RIGHT, this.getX() - Camera.x + 8, this.getY() - Camera.y, null);
				}
			}else if(dir == left_dir) {
				g.drawImage(leftPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y, null);
				if(hasGun) {
					g.drawImage(Entity.GUN_LEFT, this.getX() - Camera.x -8, this.getY() - Camera.y, null);
				}
			}else if(dir == up_dir) {
				g.drawImage(rightPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y, null);
			}
		}else {
			if(dir == right_dir) {
				g.drawImage(playerDamage,this.getX() - Camera.x,this.getY() -  Camera.y,null);
				if(hasGun) {
					g.drawImage(Entity.GUND_RIGHT, this.getX() - Camera.x + 8, this.getY() - Camera.y, null);
				}
			}else if(dir == left_dir) {
				g.drawImage(playerDamage2,this.getX() - Camera.x,this.getY() -  Camera.y,null);
				if(hasGun) {
					g.drawImage(Entity.GUND_LEFT, this.getX() - Camera.x -8, this.getY() - Camera.y, null);
				}
			}
		}
	}

}
