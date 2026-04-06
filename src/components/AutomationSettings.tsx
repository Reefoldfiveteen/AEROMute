
"use client"

import React from 'react';
import { useSoundPilot } from './SoundPilotContext';
import { Card, CardHeader, CardTitle, CardContent, CardDescription } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';
import { Bluetooth, Headphones, Zap, Radio, CheckCircle2, Waves, Activity } from 'lucide-react';
import { cn } from '@/lib/utils';

export function AutomationSettings() {
  const { autoAutomation, setAutoAutomation, deviceConnected, setDeviceConnected } = useSoundPilot();

  // Manual trigger for simulation (In real APK, this is handled by system events)
  const toggleSimulation = () => {
    const newState = !deviceConnected;
    setDeviceConnected(newState);
    // Dispatch custom event to simulate native bridge
    window.dispatchEvent(new CustomEvent('headset_event', { detail: { plugged: newState } }));
  };

  return (
    <div className="space-y-6">
      <Card className="border-border/40 shadow-xl bg-card/50 backdrop-blur-sm overflow-hidden relative">
        <div className={cn(
          "absolute top-0 left-0 right-0 h-1 transition-colors duration-500",
          deviceConnected ? "bg-accent shadow-[0_0_10px_rgba(92,219,238,0.5)]" : "bg-muted"
        )} />
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg bg-accent/10 text-accent">
                <Zap className="h-5 w-5" />
              </div>
              <div>
                <CardTitle className="text-xl font-headline">AERO Hardware Link</CardTitle>
                <CardDescription>Automatic response to physical system events</CardDescription>
              </div>
            </div>
            {deviceConnected && (
              <div className="flex items-center gap-2 px-3 py-1 rounded-full bg-accent/20 border border-accent/30 text-accent text-[10px] font-black uppercase tracking-widest animate-pulse">
                <Activity className="h-3 w-3" /> System Link Active
              </div>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-8">
          <div className="flex items-center justify-between p-5 rounded-2xl bg-black/20 border border-white/5 shadow-inner">
            <div className="space-y-1">
              <Label htmlFor="auto-pilot" className="text-sm font-black uppercase tracking-wider">Master Hardware Listen</Label>
              <p className="text-[10px] text-muted-foreground uppercase font-bold">
                Monitor System Bus for 3.5mm jack & Bluetooth events
              </p>
            </div>
            <Switch 
              id="auto-pilot" 
              checked={autoAutomation} 
              onCheckedChange={setAutoAutomation}
              className="data-[state=checked]:bg-accent"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* 3.5mm Jack Detector */}
            <div 
              onClick={toggleSimulation}
              className={cn(
                "group cursor-pointer flex flex-col gap-5 p-6 rounded-[2rem] border-2 transition-all duration-700",
                deviceConnected 
                  ? "bg-accent/5 border-accent shadow-[0_20px_40px_rgba(92,219,238,0.1)]" 
                  : "bg-muted/10 border-white/5 grayscale opacity-60 hover:grayscale-0 hover:opacity-100 hover:border-white/20"
              )}
            >
              <div className="flex items-center justify-between">
                <div className={cn(
                  "p-4 rounded-2xl transition-all duration-500",
                  deviceConnected ? "bg-accent text-accent-foreground rotate-0" : "bg-muted text-muted-foreground -rotate-12"
                )}>
                  <Headphones className="h-7 w-7" />
                </div>
                <div className={cn(
                  "text-[10px] font-black uppercase tracking-[0.2em] px-3 py-1.5 rounded-xl border transition-all",
                  deviceConnected ? "bg-accent/20 text-accent border-accent/30" : "bg-muted/20 text-muted-foreground border-white/5"
                )}>
                  {deviceConnected ? 'CONNECTED' : 'PORT EMPTY'}
                </div>
              </div>
              <div className="space-y-1">
                <h4 className="font-black text-lg uppercase tracking-tight">3.5mm Audio Port</h4>
                <p className="text-[10px] text-muted-foreground uppercase font-bold tracking-widest">
                  Auto-Calibration: {deviceConnected ? 'ACTIVE' : 'READY'}
                </p>
              </div>
              <div className="pt-2 text-[9px] font-bold text-accent uppercase tracking-tighter flex items-center gap-2">
                <div className={cn("h-1.5 w-1.5 rounded-full", deviceConnected ? "bg-accent animate-ping" : "bg-muted")} />
                Listening for ACTION_HEADSET_PLUG
              </div>
            </div>

            {/* Bluetooth Detector */}
            <div className="flex flex-col gap-5 p-6 rounded-[2rem] border-2 border-white/5 bg-muted/5 opacity-50 cursor-not-allowed">
              <div className="flex items-center justify-between">
                <div className="p-4 rounded-2xl bg-muted text-muted-foreground">
                  <Bluetooth className="h-7 w-7" />
                </div>
                <div className="text-[10px] font-black uppercase tracking-[0.2em] px-3 py-1.5 rounded-xl border border-white/5 bg-muted/20 text-muted-foreground">
                  SCANNING
                </div>
              </div>
              <div className="space-y-1">
                <h4 className="font-black text-lg uppercase tracking-tight">Bluetooth A2DP</h4>
                <p className="text-[10px] text-muted-foreground uppercase font-bold tracking-widest">Wireless Bus Monitor</p>
              </div>
              <div className="pt-2 text-[9px] font-bold text-muted-foreground uppercase tracking-tighter">
                Requires BLUETOOTH_CONNECT permission
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="p-6 rounded-2xl bg-primary/5 border border-primary/20 flex items-start gap-4">
        <Waves className="h-6 w-6 text-accent shrink-0" />
        <div className="space-y-2">
          <p className="text-xs font-black text-accent uppercase tracking-[0.2em]">Native Integration Note</p>
          <p className="text-[11px] text-muted-foreground leading-relaxed font-medium">
            AEROMute is optimized for the standard Android **AudioManager** API. When built as an APK, it utilizes a background 
            **BroadcastReceiver** to intercept system-level hardware events without requiring root or specialized firmware.
          </p>
        </div>
      </div>
    </div>
  );
}
