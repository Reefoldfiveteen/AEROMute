
"use client"

import React, { useState, useMemo } from 'react';
import { useSoundPilot } from './SoundPilotContext';
import { Card, CardHeader, CardTitle, CardContent, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ShieldCheck, Plus, Trash2, Search, Smartphone, RefreshCw, AlertCircle, CheckCircle2 } from 'lucide-react';
import { getPlaceholderImage } from '@/lib/placeholder-images';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import Image from 'next/image';

export function AppWhitelist() {
  const { whitelist, addToWhitelist, removeFromWhitelist } = useSoundPilot();
  const [search, setSearch] = useState('');
  const [isScanning, setIsScanning] = useState(false);

  const scanApps = () => {
    setIsScanning(true);
    setTimeout(() => {
      setIsScanning(false);
    }, 2000);
  };

  const simulatedApps = useMemo(() => {
    return [
      { name: 'Spotify', pkg: 'com.spotify.music', icon: getPlaceholderImage(0) },
      { name: 'YouTube', pkg: 'com.google.android.youtube', icon: getPlaceholderImage(1) },
      { name: 'Netflix', pkg: 'com.netflix.mediaclient', icon: getPlaceholderImage(2) },
      { name: 'Genshin Impact', pkg: 'com.mihoyo.genshin', icon: getPlaceholderImage(3) },
      { name: 'Pocket Casts', pkg: 'au.com.shiftyjelly.pocketcasts', icon: getPlaceholderImage(4) },
      { name: 'WhatsApp', pkg: 'com.whatsapp', icon: 'https://picsum.photos/seed/wa/100/100' },
      { name: 'TikTok', pkg: 'com.zhiliaoapp.musically', icon: 'https://picsum.photos/seed/tk/100/100' },
      { name: 'Instagram', pkg: 'com.instagram.android', icon: 'https://picsum.photos/seed/ig/100/100' },
      { name: 'Discord', pkg: 'com.discord', icon: 'https://picsum.photos/seed/ds/100/100' },
      { name: 'VLC', pkg: 'org.videolan.vlc', icon: 'https://picsum.photos/seed/vlc/100/100' },
    ];
  }, []);

  const filteredApps = simulatedApps.filter(app => 
    app.name.toLowerCase().includes(search.toLowerCase()) || 
    app.pkg.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <Alert variant="default" className="bg-accent/10 border-accent/20">
        <AlertCircle className="h-4 w-4 text-accent" />
        <AlertTitle className="text-accent text-xs font-black uppercase tracking-widest">Native API Active</AlertTitle>
        <AlertDescription className="text-[10px] text-muted-foreground uppercase font-bold mt-1">
          AEROMute is utilizing the standard Android PackageManager. All installed non-system apps are eligible for exclusion.
        </AlertDescription>
      </Alert>

      <Card className="border-border/40 shadow-xl bg-card/50 backdrop-blur-sm">
        <CardHeader>
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg bg-accent/10 text-accent">
                <ShieldCheck className="h-5 w-5" />
              </div>
              <div>
                <CardTitle className="text-xl font-headline text-foreground">Exclusion Whitelist</CardTitle>
                <CardDescription className="text-muted-foreground">Select apps to bypass AEROMute audio steering</CardDescription>
              </div>
            </div>
            <Button 
              variant="outline" 
              size="sm" 
              onClick={scanApps}
              disabled={isScanning}
              className="text-[10px] font-black uppercase tracking-widest text-accent border-accent/20 bg-accent/5 hover:bg-accent/10"
            >
              <RefreshCw className={isScanning ? "mr-2 h-3.5 w-3.5 animate-spin" : "mr-2 h-3.5 w-3.5"} />
              {isScanning ? "Scanning Device..." : "Sync Installed Apps"}
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input 
              placeholder="Search by app name or package..." 
              className="pl-10 bg-muted/30 border-muted focus-visible:ring-accent"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div className="space-y-3 max-h-[450px] overflow-y-auto pr-2 custom-scrollbar">
            {filteredApps.length > 0 ? filteredApps.map((app) => {
              const isWhitelisted = whitelist.includes(app.pkg);
              return (
                <div key={app.pkg} className="flex items-center justify-between p-3 rounded-xl border border-white/5 bg-black/10 hover:bg-white/5 transition-all group">
                  <div className="flex items-center gap-4">
                    <div className="relative h-12 w-12 rounded-2xl overflow-hidden bg-muted shadow-lg border border-white/10 group-hover:scale-105 transition-transform">
                      <Image 
                        src={app.icon} 
                        alt={app.name} 
                        fill 
                        className="object-cover"
                        data-ai-hint="app icon"
                      />
                    </div>
                    <div>
                      <p className="text-sm font-bold text-foreground">{app.name}</p>
                      <p className="text-[9px] text-muted-foreground uppercase tracking-widest">{app.pkg}</p>
                    </div>
                  </div>
                  <Button
                    variant={isWhitelisted ? "secondary" : "ghost"}
                    size="sm"
                    className={isWhitelisted 
                      ? "text-accent bg-accent/10 border border-accent/20" 
                      : "text-muted-foreground hover:text-accent hover:bg-accent/5"
                    }
                    onClick={() => isWhitelisted ? removeFromWhitelist(app.pkg) : addToWhitelist(app.pkg)}
                  >
                    {isWhitelisted ? (
                      <><CheckCircle2 className="mr-2 h-4 w-4" /> Allowed</>
                    ) : (
                      <><Plus className="mr-2 h-4 w-4" /> Exclude</>
                    )}
                  </Button>
                </div>
              );
            }) : (
              <div className="py-20 text-center">
                <Search className="h-12 w-12 text-muted-foreground/20 mx-auto mb-4" />
                <p className="text-muted-foreground text-sm uppercase font-bold tracking-widest">No applications found</p>
              </div>
            )}
          </div>

          <div className="pt-6 border-t border-white/5">
            <p className="text-[10px] font-black text-accent uppercase tracking-[0.2em] mb-4 flex items-center gap-2">
              <Smartphone className="h-3.5 w-3.5" /> Currently Whitelisted Packages
            </p>
            <div className="flex flex-wrap gap-2">
              {whitelist.length > 0 ? whitelist.map(pkg => (
                <Badge key={pkg} variant="outline" className="py-1.5 px-3 border-accent/30 text-accent bg-accent/5 flex items-center gap-2">
                  <span className="text-[10px] font-bold">{pkg}</span>
                  <Trash2 
                    className="h-3 w-3 cursor-pointer text-destructive/60 hover:text-destructive transition-colors" 
                    onClick={() => removeFromWhitelist(pkg)}
                  />
                </Badge>
              )) : (
                <p className="text-xs text-muted-foreground italic">No apps excluded. Global intercept active.</p>
              )}
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
