package me.flashyreese.mods.sodiumextra.client;

import com.google.common.collect.EvictingQueue;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Queue;
import java.util.stream.IntStream;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = "ribbon")
public class ClientTickHandler {
    private static int averageFps, lowestFps, highestFps;
    private static final Queue<Integer> fpsQueue = EvictingQueue.create(200);

    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent event) {
        // The ClientTickEvent is fired twice per tick, once at the start and once at the end.
        if (event.phase == TickEvent.Phase.END)
            return;

        final int currentFPS = Minecraft.getInstance().getFps();
        fpsQueue.add(currentFPS);

        final int[] fpsArray = fpsQueue.stream().mapToInt(Integer::intValue).toArray();
        averageFps = (int) IntStream.of(fpsArray).average().orElse(0);
        lowestFps = IntStream.of(fpsArray).min().orElse(0);
        highestFps = IntStream.of(fpsArray).max().orElse(0);
    }

    public static int getAverageFps() {
        return averageFps;
    }

    public static int getLowestFps() {
        return lowestFps;
    }

    public static int getHighestFps() {
        return highestFps;
    }
}
