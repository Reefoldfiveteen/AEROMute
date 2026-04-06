
"use client"

import React, { createContext, useContext, useState, useEffect, useRef } from 'react';

type SoundPilotState = {
  forceMuteEnabled: boolean;
  setForceMuteEnabled: (val: boolean) => void;
  volume: number;
  setVolume: (val: number) => void;
  isMuted: boolean;
  setIsMuted: (val: boolean) => void;
  whitelist: string[];
  addToWhitelist: (pkg: string) => void;
  removeFromWhitelist: (pkg: string) => void;
  autoAutomation: boolean;
  setAutoAutomation: (val: boolean) => void;
  deviceConnected: boolean; // 3.5mm Jack status
  setDeviceConnected: (val: boolean) => void;
  isFloatingVisible: boolean;
  setIsFloatingVisible: (val: boolean) => void;
  isServiceActive: boolean;
  setIsServiceActive: (val: boolean) => void;
};

const SoundPilotContext = createContext<SoundPilotState | undefined>(undefined);

export function SoundPilotProvider({ children }: { children: React.ReactNode }) {
  const [forceMuteEnabled, setForceMuteEnabled] = useState(true);
  const [volume, setVolume] = useState(65);
  const [isMuted, setIsMuted] = useState(false);
  const [whitelist, setWhitelist] = useState<string[]>(['com.android.phone', 'com.google.android.dialer', 'com.android.systemui']);
  const [autoAutomation, setAutoAutomation] = useState(true);
  const [deviceConnected, setDeviceConnected] = useState(false); 
  const [isFloatingVisible, setIsFloatingVisible] = useState(true);
  const [isServiceActive, setIsServiceActive] = useState(true);
  
  const prevVolumeRef = useRef(65);
  const prevForceMuteRef = useRef(true);

  useEffect(() => {
    const saved = localStorage.getItem('AEROMute_Settings_V5');
    if (saved) {
      try {
        const data = JSON.parse(saved);
        if (typeof data.forceMuteEnabled === 'boolean') setForceMuteEnabled(data.forceMuteEnabled);
        if (typeof data.volume === 'number') setVolume(data.volume);
        if (typeof data.isMuted === 'boolean') setIsMuted(data.isMuted);
        if (Array.isArray(data.whitelist)) setWhitelist(data.whitelist);
        if (typeof data.isFloatingVisible === 'boolean') setIsFloatingVisible(data.isFloatingVisible);
      } catch (e) {
        console.error("Failed to parse AEROMute settings", e);
      }
    }

    const handleNativeHardwareEvent = (e: any) => {
      if (e.detail && typeof e.detail.plugged === 'boolean') {
        setDeviceConnected(e.detail.plugged);
      }
    };

    window.addEventListener('headset_event', handleNativeHardwareEvent);
    return () => window.removeEventListener('headset_event', handleNativeHardwareEvent);
  }, []);

  useEffect(() => {
    const settings = { forceMuteEnabled, volume, isMuted, whitelist, isFloatingVisible };
    localStorage.setItem('AEROMute_Settings_V5', JSON.stringify(settings));
  }, [forceMuteEnabled, volume, isMuted, whitelist, isFloatingVisible]);

  useEffect(() => {
    if (autoAutomation && deviceConnected) {
      prevVolumeRef.current = volume;
      prevForceMuteRef.current = forceMuteEnabled;
      
      setForceMuteEnabled(false); 
      setVolume(30); 
      setIsMuted(false);
    } else if (autoAutomation && !deviceConnected && prevForceMuteRef.current === true) {
      setForceMuteEnabled(true);
      setVolume(prevVolumeRef.current);
    }
  }, [deviceConnected, autoAutomation]);

  const addToWhitelist = (pkg: string) => setWhitelist(prev => [...new Set([...prev, pkg])]);
  const removeFromWhitelist = (pkg: string) => setWhitelist(prev => prev.filter(p => p !== pkg));

  return (
    <SoundPilotContext.Provider value={{
      forceMuteEnabled, setForceMuteEnabled,
      volume, setVolume,
      isMuted, setIsMuted,
      whitelist, addToWhitelist, removeFromWhitelist,
      autoAutomation, setAutoAutomation,
      deviceConnected, setDeviceConnected,
      isFloatingVisible, setIsFloatingVisible,
      isServiceActive, setIsServiceActive
    }}>
      {children}
    </SoundPilotContext.Provider>
  );
}

export function useSoundPilot() {
  const context = useContext(SoundPilotContext);
  if (!context) throw new Error('useSoundPilot must be used within a SoundPilotProvider');
  return context;
}
