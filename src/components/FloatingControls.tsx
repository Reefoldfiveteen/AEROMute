
"use client"

import React, { useState, useEffect, useRef } from 'react';
import { useSoundPilot } from './SoundPilotContext';
import { Slider } from '@/components/ui/slider';
import { Volume2, VolumeX, GripVertical, Minimize2, ChevronUp, ChevronDown, Bell, X, Maximize2 } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';

export function FloatingControls() {
  const { volume, setVolume, isMuted, setIsMuted, isFloatingVisible, setIsFloatingVisible } = useSoundPilot();
  
  const [position, setPosition] = useState({ x: 20, y: 150 });
  const [isDragging, setIsDragging] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [showNotificationPopup, setShowNotificationPopup] = useState(false);
  
  const dragStartOffset = useRef({ x: 0, y: 0 });

  useEffect(() => {
    const savedPos = localStorage.getItem('AEROMute_ShifterPos_V5');
    if (savedPos) {
      try {
        const parsed = JSON.parse(savedPos);
        const safeX = Math.max(0, Math.min(window.innerWidth - 80, parsed.x));
        const safeY = Math.max(0, Math.min(window.innerHeight - (isMinimized ? 100 : 520), parsed.y));
        setPosition({ x: safeX, y: safeY });
      } catch (e) {
        console.error("Failed to parse shifter position", e);
      }
    }
  }, [isMinimized]);

  useEffect(() => {
    const handleMove = (clientX: number, clientY: number) => {
      if (!isDragging) return;
      
      const newX = Math.max(0, Math.min(window.innerWidth - 80, clientX - dragStartOffset.current.x));
      const newY = Math.max(0, Math.min(window.innerHeight - (isMinimized ? 100 : 520), clientY - dragStartOffset.current.y));
      
      setPosition({ x: newX, y: newY });
    };

    const handleMouseMove = (e: MouseEvent) => handleMove(e.clientX, e.clientY);
    const handleTouchMove = (e: TouchEvent) => {
      if (e.touches.length > 0) {
        handleMove(e.touches[0].clientX, e.touches[0].clientY);
      }
    };

    const handleEnd = () => {
      if (isDragging) {
        setIsDragging(false);
        localStorage.setItem('AEROMute_ShifterPos_V5', JSON.stringify(position));
      }
    };

    if (isDragging) {
      window.addEventListener('mousemove', handleMouseMove);
      window.addEventListener('mouseup', handleEnd);
      window.addEventListener('touchmove', handleTouchMove, { passive: false });
      window.addEventListener('touchend', handleEnd);
    }
    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleEnd);
      window.removeEventListener('touchmove', handleTouchMove);
      window.removeEventListener('touchend', handleEnd);
    };
  }, [isDragging, position, isMinimized]);

  const onDragStart = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsDragging(true);
    dragStartOffset.current = { 
      x: e.clientX - position.x, 
      y: e.clientY - position.y 
    };
  };

  const onTouchStart = (e: React.TouchEvent) => {
    const touch = e.touches[0];
    setIsDragging(true);
    dragStartOffset.current = { 
      x: touch.clientX - position.x, 
      y: touch.clientY - position.y 
    };
  };

  const adjustVolume = (delta: number) => {
    const newVal = Math.min(100, Math.max(0, volume + delta));
    setVolume(newVal);
    if (newVal > 0) setIsMuted(false);
  };

  const handleFullHide = () => {
    setIsFloatingVisible(false);
    setShowNotificationPopup(true);
    setTimeout(() => setShowNotificationPopup(false), 5000);
  };

  if (!isFloatingVisible) {
    return (
      <>
        {showNotificationPopup && (
          <div className="fixed top-4 left-1/2 -translate-x-1/2 z-[10000] w-full max-w-sm px-4 animate-in fade-in slide-in-from-top-4 duration-500">
            <div className="glass-morphism rounded-2xl p-4 flex items-center justify-between border-accent/20 shadow-2xl cursor-pointer" onClick={() => setIsFloatingVisible(true)}>
              <div className="flex items-center gap-3">
                <div className="h-8 w-8 rounded-lg bg-accent/20 flex items-center justify-center text-accent">
                  <Bell className="h-4 w-4" />
                </div>
                <div>
                  <p className="text-[10px] font-black text-accent uppercase tracking-widest">AEROMute Active</p>
                  <p className="text-xs font-bold text-foreground">Tap to restore floating shifter</p>
                </div>
              </div>
              <button onClick={(e) => { e.stopPropagation(); setShowNotificationPopup(false); }} className="text-muted-foreground hover:text-white transition-colors">
                <X className="h-4 w-4" />
              </button>
            </div>
          </div>
        )}
      </>
    );
  }

  if (isMinimized) {
    return (
      <div 
        className={cn(
          "fixed z-[9999] select-none touch-none",
          "floating-widget glass-morphism rounded-full flex items-center justify-center p-3 w-16 h-16 cursor-pointer",
          isDragging ? "opacity-90 scale-110" : "shadow-2xl transition-transform"
        )}
        style={{ left: position.x, top: position.y }}
        onMouseDown={onDragStart}
        onTouchStart={onTouchStart}
        onClick={() => !isDragging && setIsMinimized(false)}
      >
        <div className={cn(
          "h-10 w-10 rounded-full flex items-center justify-center border-2 transition-colors",
          isMuted ? "border-destructive bg-destructive/10 text-destructive" : "border-accent bg-accent/10 text-accent"
        )}>
          {isMuted ? <VolumeX className="h-5 w-5" /> : <Volume2 className="h-5 w-5" />}
        </div>
      </div>
    );
  }

  return (
    <div 
      className={cn(
        "fixed z-[9999] select-none touch-none",
        "floating-widget glass-morphism rounded-[2.5rem] flex flex-col items-center py-6 px-4 w-20 gap-5 h-[500px]",
        isDragging ? "opacity-90 scale-[1.02] ring-2 ring-accent/40" : "shadow-2xl transition-transform duration-300"
      )}
      style={{ left: position.x, top: position.y }}
    >
      <div 
        onMouseDown={onDragStart}
        onTouchStart={onTouchStart}
        className="cursor-grab active:cursor-grabbing p-2 text-muted-foreground/30 hover:text-accent transition-colors w-full flex flex-col items-center gap-0.5"
      >
        <GripVertical className="h-6 w-6 rotate-90" />
        <GripVertical className="h-6 w-6 rotate-90 -mt-3.5" />
      </div>

      <div className="bg-black/40 border border-white/5 rounded-2xl w-full py-2 flex flex-col items-center shadow-inner">
        <span className="text-[9px] font-black text-muted-foreground uppercase tracking-tighter mb-0.5">Gain</span>
        <span className={cn(
          "text-xs font-black tabular-nums transition-colors",
          isMuted ? "text-destructive" : "text-accent"
        )}>
          {isMuted ? 'OFF' : `${volume}%`}
        </span>
      </div>

      <div className="flex-1 flex flex-col items-center gap-3 w-full">
        <button 
          onClick={() => adjustVolume(5)} 
          className="h-10 w-full flex items-center justify-center rounded-2xl bg-white/5 hover:bg-accent/20 hover:text-accent transition-all active:scale-95"
        >
          <ChevronUp className="h-5 w-5" />
        </button>
        
        <div className="flex-1 w-full flex justify-center py-2">
          <Slider
            orientation="vertical"
            value={[isMuted ? 0 : volume]}
            max={100}
            step={1}
            onValueChange={(v) => {
              setVolume(v[0]);
              if (v[0] > 0) setIsMuted(false);
            }}
            className="h-full"
          />
        </div>

        <button 
          onClick={() => adjustVolume(-5)} 
          className="h-10 w-full flex items-center justify-center rounded-2xl bg-white/5 hover:bg-accent/20 hover:text-accent transition-all active:scale-95"
        >
          <ChevronDown className="h-5 w-5" />
        </button>
      </div>

      <div className="flex flex-col gap-4 w-full items-center border-t border-white/5 pt-6">
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger asChild>
              <button
                className={cn(
                  "h-12 w-12 rounded-2xl transition-all duration-300 shadow-xl flex items-center justify-center",
                  isMuted ? "text-destructive bg-destructive/10 border border-destructive/20" : "text-accent bg-accent/10 border border-accent/20"
                )}
                onClick={() => setIsMuted(!isMuted)}
              >
                {isMuted ? <VolumeX className="h-6 w-6" /> : <Volume2 className="h-6 w-6" />}
              </button>
            </TooltipTrigger>
            <TooltipContent side="left" className="bg-card border-accent/20 text-[10px] font-black uppercase tracking-widest">
              Quick Mute
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
        
        <div className="flex items-center gap-1">
          <button 
            onClick={() => setIsMinimized(true)}
            className="p-2 rounded-xl hover:bg-accent/10 text-muted-foreground/40 hover:text-accent transition-all"
            title="Minimize to Bubble"
          >
            <Minimize2 className="h-4 w-4" />
          </button>
          <button 
            onClick={handleFullHide}
            className="p-2 rounded-xl hover:bg-accent/10 text-muted-foreground/40 hover:text-accent transition-all"
            title="Hide Completely"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  );
}
