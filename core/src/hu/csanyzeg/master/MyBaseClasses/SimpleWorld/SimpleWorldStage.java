package hu.csanyzeg.master.MyBaseClasses.SimpleWorld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.Viewport;


import hu.csanyzeg.master.MyBaseClasses.Game.MyGame;
import hu.csanyzeg.master.MyBaseClasses.Scene2D.WorldActor;
import hu.csanyzeg.master.MyBaseClasses.Timers.TickTimer;
import hu.csanyzeg.master.MyBaseClasses.Timers.TickTimerListener;
import hu.csanyzeg.master.MyBaseClasses.WorldHelper.HelperStage;

public class SimpleWorldStage extends HelperStage<SimpleWorld, SimpleWorldHelper> {

    private long lastWorldMs = 0;
    protected int iterations = 1;
    protected float iterationPerSec = 111f;

    protected SimpleWorldDebugRenderer simpleWorldDebugRenderer;

    public float getIterationPerSec() {
        return iterationPerSec;
    }

    public void setIterationPerSec(float iterationPerSec) {
        this.iterationPerSec = iterationPerSec;
    }


    public SimpleWorldStage(Viewport viewport, MyGame game) {
        super(viewport, game);
        world  = new SimpleWorld();
        simpleWorldDebugRenderer = new SimpleWorldDebugRenderer();
        if (game.debug){
            addTimer(new TickTimer(1.017f, true, new TickTimerListener() {
                @Override
                public void onRepeat(TickTimer sender) {
                    Gdx.app.log("world", "DT world step: " + (lastWorldMs / 1000000f) +" ms; ET world & SWstage: " + elapsedTime + " \tWorld iterations per delta: " + iterations + " Body count: " + world.getBodyCount());
                }
            }));
        };

        world.setContactListener(new SimpleWorldContactListener() {
            @Override
            public void beginContact(SimpleWorld world, SimpleContact contact) {
                if (contact.bodyA.worldData instanceof SimpleWorldHelper && contact.bodyB.worldData instanceof SimpleWorldHelper) {
                    SimpleWorldHelper helperA = ((SimpleWorldHelper) contact.bodyA.worldData);
                    SimpleWorldHelper helperB = ((SimpleWorldHelper) contact.bodyB.worldData);
                    for(SimpleBodyContactListener myContactListener : helperA.contactListeners){
                        myContactListener.beginContact(contact, helperA, helperB);
                    }
                    for(SimpleBodyContactListener myContactListener : helperB.contactListeners){
                        myContactListener.beginContact(contact, helperB, helperA);
                    }
                }
            }

            @Override
            public void endContact(SimpleWorld world, SimpleContact contact) {
                if (contact.bodyA.worldData instanceof SimpleWorldHelper && contact.bodyB.worldData instanceof SimpleWorldHelper) {
                    SimpleWorldHelper helperA = ((SimpleWorldHelper) contact.bodyA.worldData);
                    SimpleWorldHelper helperB = ((SimpleWorldHelper) contact.bodyB.worldData);
                    for(SimpleBodyContactListener myContactListener : helperA.contactListeners){
                        myContactListener.endContact(contact, helperA, helperB);
                    }
                    for(SimpleBodyContactListener myContactListener : helperB.contactListeners){
                        myContactListener.endContact(contact, helperB, helperA);
                    }
                }
            }
        });
    }

    @Override
    public void act(float delta) {
        long m = TimeUtils.nanoTime();
        if (delta > 0.1f){
            delta = 0.1f;
        }
        iterations = 1 + (int)(delta*iterationPerSec);
        world.step(delta, iterations, 10);
        lastWorldMs = TimeUtils.nanoTime() - m;
        super.act(delta);
    }

    @Override
    public void draw() {
        super.draw();
        if(game.debug) simpleWorldDebugRenderer.render(world, Gdx.graphics.getDeltaTime(), getCamera().combined);
    }

    public static SimpleBody getBody(WorldActor actor){
        return ((SimpleBody)actor.getActorWorldHelper().getBody());
    }
}
