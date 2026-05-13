package com.example.worldcup2026.data.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010!\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001a\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004J\u0016\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00050\nH\u0002J\u0014\u0010\u000b\u001a\u0004\u0018\u00010\f2\b\u0010\r\u001a\u0004\u0018\u00010\u0005H\u0002J4\u0010\u000e\u001a\u0004\u0018\u00010\f2\u0018\u0010\u000f\u001a\u0014\u0012\u0004\u0012\u00020\u0011\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00040\u00102\u0006\u0010\u0013\u001a\u00020\u00112\u0006\u0010\u0014\u001a\u00020\u0015H\u0002J\u0014\u0010\u0016\u001a\u0004\u0018\u00010\f2\b\u0010\r\u001a\u0004\u0018\u00010\u0005H\u0002J2\u0010\u0017\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00050\n2\u0006\u0010\u0018\u001a\u00020\u00152\b\u0010\u0019\u001a\u0004\u0018\u00010\f2\b\u0010\u001a\u001a\u0004\u0018\u00010\fH\u0002\u00a8\u0006\u001b"}, d2 = {"Lcom/example/worldcup2026/data/util/KnockoutCalculator;", "", "()V", "calculateKnockoutMatches", "", "Lcom/example/worldcup2026/data/model/Match;", "allMatches", "fillWinners", "", "matches", "", "getLoser", "Lcom/example/worldcup2026/data/model/Team;", "match", "getTeamAt", "standings", "", "", "Lcom/example/worldcup2026/data/util/TeamStats;", "group", "pos", "", "getWinner", "updateKnockoutMatch", "id", "home", "away", "app_debug"})
public final class KnockoutCalculator {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.worldcup2026.data.util.KnockoutCalculator INSTANCE = null;
    
    private KnockoutCalculator() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.worldcup2026.data.model.Match> calculateKnockoutMatches(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.worldcup2026.data.model.Match> allMatches) {
        return null;
    }
    
    private final com.example.worldcup2026.data.model.Team getTeamAt(java.util.Map<java.lang.String, ? extends java.util.List<com.example.worldcup2026.data.util.TeamStats>> standings, java.lang.String group, int pos) {
        return null;
    }
    
    private final void updateKnockoutMatch(java.util.List<com.example.worldcup2026.data.model.Match> matches, int id, com.example.worldcup2026.data.model.Team home, com.example.worldcup2026.data.model.Team away) {
    }
    
    private final void fillWinners(java.util.List<com.example.worldcup2026.data.model.Match> matches) {
    }
    
    private final com.example.worldcup2026.data.model.Team getWinner(com.example.worldcup2026.data.model.Match match) {
        return null;
    }
    
    private final com.example.worldcup2026.data.model.Team getLoser(com.example.worldcup2026.data.model.Match match) {
        return null;
    }
}