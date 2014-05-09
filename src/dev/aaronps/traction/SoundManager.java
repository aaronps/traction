package dev.aaronps.traction;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager
{
    public static final NoSoundPlayer nosound_player = new NoSoundPlayer();
    public static final NormalPlayer normal_player = new NormalPlayer();
    public static Player player = nosound_player;

    static interface Player
    {
        void play(final int soundId);
        boolean activate();
        void deactivate();
        boolean load(final Context context);
    }

    public static class NoSoundPlayer implements Player
    {
        @Override
        public void play(final int soundId)
        {
        }

        @Override
        public boolean activate()
        {
            return true;
        }

        @Override
        public void deactivate()
        {
        }

        @Override
        public boolean load(final Context context)
        {
            return true;
        }
    }

    public static class NormalPlayer implements Player
    {
        SoundPool soundPool = null;

        @Override
        public boolean activate()
        {
            soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
            return soundPool != null;
        }

        @Override
        public void deactivate()
        {
            if (soundPool != null)
            {
                soundPool.release();
                soundPool = null;
            }
        }

        @Override
        public boolean load(final Context context)
        {
            final AssetManager am = context.getAssets();
            try
            {
                GameResources.explosionSound = soundPool.load(am.openFd("DeathFlashCut.ogg"), 0);
                GameResources.shieldHitSound = soundPool.load(am.openFd("shield-hit2.ogg"), 0);
            }
            catch (IOException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        public void play(final int soundId)
        {
            soundPool.play(soundId, 0.8f, 0.8f, 0, 0, 1.0f);
        }
    }

    public static boolean init(Context context)
    {
        player = nosound_player;
        if (normal_player.activate() && normal_player.load(context))
        {
            player = normal_player;
            return true;
        }
        return false;
    }

    public static void deinit()
    {
        player.deactivate();
        player = nosound_player;
    }

}
