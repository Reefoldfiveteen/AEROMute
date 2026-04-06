
"use client"

import React, { useState, useEffect } from 'react';
import { SoundPilotProvider, useSoundPilot } from '@/components/SoundPilotContext';
import { FloatingControls } from '@/components/FloatingControls';
import { AppWhitelist } from '@/components/AppWhitelist';
import { AutomationSettings } from '@/components/AutomationSettings';
import { Card, CardContent } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Switch } from '@/components/ui/switch';
import { Slider } from '@/components/ui/slider';
import { Label } from '@/components/ui/label';
import { 
  Volume2, 
  VolumeX, 
  Settings2, 
  ShieldCheck, 
  Zap, 
  Cpu, 
  LayoutGrid,
  Radio,
  Wifi,
  Clock,
  Waves,
  MessageSquare,
  Smartphone,
  Unplug
} from 'lucide-react';
import { cn } from '@/lib/utils';

function DashboardContent() {
  const { 
    forceMuteEnabled, setForceMuteEnabled, 
    volume, setVolume, 
    isMuted, setIsMuted,
    isFloatingVisible, setIsFloatingVisible,
    deviceConnected
  } = useSoundPilot();

  const [currentTime, setCurrentTime] = useState<string>('--:--');
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const updateTime = () => {
      const now = new Date();
      setCurrentTime(now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
    };
    updateTime();
    const timer = setInterval(updateTime, 1000);
    return () => clearInterval(timer);
  }, []);

  return (
    <main className="min-h-screen pb-24 lg:pb-12 pt-6 px-4 max-w-7xl mx-auto space-y-6">
      {/* Top Status Bar Decoration */}
      <div className="flex items-center justify-between px-6 py-2 mb-4 bg-black/40 rounded-full border border-white/5 backdrop-blur-md">
        <div className="flex items-center gap-6 text-[10px] font-bold tracking-widest text-muted-foreground uppercase">
          <div className="flex items-center gap-1.5"><Wifi className="h-3 w-3" /> System Standard</div>
          <div className="flex items-center gap-1.5">
            <ShieldCheck className={cn("h-3 w-3", deviceConnected ? "text-accent" : "text-muted-foreground")} /> 
            {deviceConnected ? 'Hardware Linked' : 'Awaiting Input'}
          </div>
        </div>
        <div className="flex items-center gap-4 text-[10px] font-bold tracking-widest text-accent uppercase">
          <div className="flex items-center gap-1.5">
            <Clock className="h-3 w-3" /> 
            {currentTime}
          </div>
        </div>
      </div>

      {/* Header Section */}
      <header className="flex flex-col md:flex-row items-center justify-between gap-6 mb-10">
        <div className="flex items-center gap-5">
          <div className="h-16 w-16 rounded-[2rem] bg-primary flex items-center justify-center shadow-2xl shadow-primary/40 border-2 border-primary/50 overflow-hidden relative group">
            <Radio className="h-9 w-9 text-accent animate-pulse-accent z-10" />
            <div className="absolute inset-0 bg-gradient-to-tr from-accent/30 via-transparent to-transparent opacity-50 group-hover:opacity-100 transition-opacity" />
          </div>
          <div>
            <h1 className="text-4xl font-black tracking-tighter font-headline text-foreground bg-clip-text text-transparent bg-gradient-to-r from-white to-white/60">AEROMute</h1>
            <p className="text-muted-foreground text-[10px] font-black flex items-center gap-2 mt-1 uppercase tracking-widest">
              <span className={cn("flex h-2 w-2 rounded-full", deviceConnected ? "bg-accent animate-pulse" : "bg-muted")} />
              v5.0 • Standard API • Non-Root
            </p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          {!isFloatingVisible && (
            <button 
              onClick={() => setIsFloatingVisible(true)}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-accent/10 border border-accent/20 text-accent text-[10px] font-black uppercase tracking-widest hover:bg-accent/20 transition-all animate-pulse"
            >
              <MessageSquare className="h-3.5 w-3.5" /> Restore Shifter
            </button>
          )}
          <div className="hidden lg:flex items-center gap-3 bg-card/40 p-1.5 rounded-2xl border border-white/5 backdrop-blur-sm">
            <div className="px-5 py-2 rounded-xl bg-primary/20 text-accent text-[10px] font-black uppercase tracking-[0.2em] border border-accent/20 flex items-center gap-3 shadow-inner">
              <ShieldCheck className="h-3.5 w-3.5" /> Core Service Active
            </div>
          </div>
        </div>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left Column - Main Steering Controls */}
        <div className="lg:col-span-4 space-y-6">
          <Card className={cn(
            "border-none shadow-2xl relative overflow-hidden transition-all duration-700 rounded-[3rem]",
            forceMuteEnabled ? "bg-primary/30 ring-1 ring-accent/30" : "bg-card/40"
          )}>
            <div className={cn(
              "absolute -top-24 -right-24 h-64 w-64 opacity-20 transition-all duration-700 blur-[80px]",
              forceMuteEnabled ? "bg-accent scale-150" : "bg-primary"
            )} />
            
            <CardContent className="p-10 relative z-10 flex flex-col items-center text-center gap-8">
              <div className="space-y-1">
                <h2 className="text-[10px] font-black uppercase tracking-[0.3em] text-muted-foreground">Master Status</h2>
                <p className="text-2xl font-black font-headline tracking-tight uppercase">
                  {forceMuteEnabled ? 'Audio Redirect' : 'Hardware Direct'}
                </p>
              </div>

              <div 
                className="relative group cursor-pointer" 
                onClick={() => setForceMuteEnabled(!forceMuteEnabled)}
              >
                <div className={cn(
                  "h-56 w-56 rounded-full flex flex-col items-center justify-center border-[6px] transition-all duration-700 transform group-hover:scale-[1.02] shadow-2xl",
                  forceMuteEnabled 
                    ? "border-accent bg-accent/5 shadow-accent/20" 
                    : "border-white/10 bg-black/20"
                )}>
                  <div className={cn(
                    "h-40 w-40 rounded-full flex items-center justify-center border-2 transition-all duration-700",
                    forceMuteEnabled ? "border-accent/50 bg-accent/10 shadow-[inset_0_0_30px_rgba(92,219,238,0.2)]" : "border-white/5 bg-white/5"
                  )}>
                    {deviceConnected && !forceMuteEnabled ? (
                      <Unplug className="h-16 w-16 text-accent animate-pulse" />
                    ) : (
                      <Waves className={cn(
                        "h-16 w-16 transition-all duration-700",
                        forceMuteEnabled ? "text-accent scale-110" : "text-white/20"
                      )} />
                    )}
                  </div>
                  <div className="absolute -bottom-2">
                    <span className={cn(
                      "text-[10px] font-black uppercase tracking-[0.2em] px-5 py-1.5 rounded-full border shadow-lg transition-all duration-500",
                      forceMuteEnabled 
                        ? "bg-accent text-accent-foreground border-accent shadow-accent/40" 
                        : "bg-secondary text-muted-foreground border-white/10"
                    )}>
                      {forceMuteEnabled ? 'STEERING ACTIVE' : 'BYPASS MODE'}
                    </span>
                  </div>
                </div>
              </div>

              <div className="w-full flex flex-col gap-8 pt-6">
                <div className="flex items-center justify-between p-4 rounded-2xl bg-black/20 border border-white/5 shadow-inner">
                  <div className="text-left">
                    <p className="text-xs font-black uppercase tracking-wider text-foreground">Global Intercept</p>
                    <p className="text-[9px] font-medium text-muted-foreground uppercase tracking-widest">AERO Overlays</p>
                  </div>
                  <Switch 
                    checked={forceMuteEnabled} 
                    onCheckedChange={setForceMuteEnabled}
                    className="data-[state=checked]:bg-accent"
                  />
                </div>
                
                <div className="space-y-4 px-2">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2.5">
                      <div className={cn(
                        "p-1.5 rounded-lg transition-colors",
                        isMuted ? "bg-destructive/10 text-destructive" : "bg-accent/10 text-accent"
                      )}>
                        {isMuted ? <VolumeX className="h-4 w-4" /> : <Volume2 className="h-4 w-4" />}
                      </div>
                      <span className="text-xs font-black uppercase tracking-widest">Gain Control</span>
                    </div>
                    <span className="text-sm font-black tabular-nums text-accent bg-accent/10 px-3 py-0.5 rounded-md border border-accent/20">
                      {isMuted ? 'MUTED' : `${volume}%`}
                    </span>
                  </div>
                  <Slider 
                    value={[isMuted ? 0 : volume]} 
                    max={100} 
                    step={1}
                    onValueChange={(v) => {
                      setVolume(v[0]);
                      if (v[0] > 0) setIsMuted(false);
                    }}
                    className="py-2"
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right Column - Tabs */}
        <div className="lg:col-span-8">
          <Tabs defaultValue="automation" className="w-full">
            <TabsList className="bg-card/40 backdrop-blur-md border border-white/5 p-2 rounded-2xl mb-8 w-full flex justify-start h-auto gap-2 overflow-x-auto no-scrollbar">
              <TabsTrigger value="automation" className="rounded-xl py-3 px-6 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground flex items-center gap-3 text-xs font-black uppercase tracking-widest shrink-0">
                <Zap className="h-4 w-4" /> Hardware Link
              </TabsTrigger>
              <TabsTrigger value="whitelist" className="rounded-xl py-3 px-6 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground flex items-center gap-3 text-xs font-black uppercase tracking-widest shrink-0">
                <ShieldCheck className="h-4 w-4" /> App Exclusion
              </TabsTrigger>
              <TabsTrigger value="settings" className="rounded-xl py-3 px-6 data-[state=active]:bg-primary data-[state=active]:text-primary-foreground flex items-center gap-3 text-xs font-black uppercase tracking-widest shrink-0">
                <Settings2 className="h-4 w-4" /> Core System
              </TabsTrigger>
            </TabsList>
            
            <TabsContent value="whitelist" className="mt-0 outline-none">
              <AppWhitelist />
            </TabsContent>
            
            <TabsContent value="automation" className="mt-0 outline-none">
              <AutomationSettings />
            </TabsContent>

            <TabsContent value="settings" className="mt-0 outline-none">
              <Card className="border-white/5 shadow-2xl bg-card/40 backdrop-blur-xl rounded-[2.5rem]">
                <CardContent className="p-10 space-y-10">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
                    <div className="space-y-6">
                      <div className="flex items-center gap-2 mb-2">
                        <LayoutGrid className="h-4 w-4 text-accent" />
                        <h3 className="text-xs font-black uppercase tracking-[0.2em] text-accent">Overlay Engine</h3>
                      </div>
                      <div className="space-y-8">
                        <div className="flex items-center justify-between">
                          <div className="space-y-0.5">
                            <Label className="text-sm font-bold uppercase tracking-tight">Floating Shifter</Label>
                            <p className="text-[10px] text-muted-foreground uppercase font-bold">System-wide audio controller</p>
                          </div>
                          <Switch 
                            checked={isFloatingVisible} 
                            onCheckedChange={setIsFloatingVisible}
                            className="data-[state=checked]:bg-accent" 
                          />
                        </div>
                        <div className="flex items-center justify-between">
                          <div className="space-y-0.5">
                            <Label className="text-sm font-bold uppercase tracking-tight">Real-time Delta</Label>
                            <p className="text-[10px] text-muted-foreground uppercase font-bold">Percentage gain feedback</p>
                          </div>
                          <Switch checked className="data-[state=checked]:bg-accent" />
                        </div>
                      </div>
                    </div>
                    
                    <div className="space-y-6">
                      <div className="flex items-center gap-2 mb-2">
                        <Cpu className="h-4 w-4 text-accent" />
                        <h3 className="text-xs font-black uppercase tracking-[0.2em] text-accent">Process Core</h3>
                      </div>
                      <div className="space-y-8">
                        <div className="flex items-center justify-between">
                          <div className="space-y-0.5">
                            <Label className="text-sm font-bold uppercase tracking-tight">Wake Lock Persistence</Label>
                            <p className="text-[10px] text-muted-foreground uppercase font-bold">Prevents background sleep</p>
                          </div>
                          <Switch checked className="data-[state=checked]:bg-accent" />
                        </div>
                        <div className="flex items-center justify-between">
                          <div className="space-y-0.5">
                            <Label className="text-sm font-bold uppercase tracking-tight">Sync DND Policy</Label>
                            <p className="text-[10px] text-muted-foreground uppercase font-bold">Follow Android Do Not Disturb</p>
                          </div>
                          <Switch className="data-[state=checked]:bg-accent" />
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div className="p-8 rounded-[2rem] bg-destructive/5 border border-destructive/20 flex flex-col md:flex-row items-center justify-between gap-6 shadow-inner">
                    <div className="space-y-1 text-center md:text-left">
                      <p className="text-sm font-black uppercase tracking-widest text-destructive">Uninstall AERO Core</p>
                      <p className="text-[10px] text-muted-foreground uppercase font-bold">Stop all background intercept services</p>
                    </div>
                    <button className="px-8 py-3 rounded-2xl bg-destructive/10 text-destructive text-[10px] font-black uppercase tracking-[0.2em] hover:bg-destructive hover:text-white transition-all border border-destructive/20">
                      Disable Service
                    </button>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </div>

      <FloatingControls />
    </main>
  );
}

export default function AEROMuteApp() {
  return (
    <SoundPilotProvider>
      <div className="min-h-screen bg-background text-foreground bg-[radial-gradient(circle_at_top_right,rgba(51,102,153,0.15),transparent_60%)]">
        <DashboardContent />
      </div>
    </SoundPilotProvider>
  );
}
