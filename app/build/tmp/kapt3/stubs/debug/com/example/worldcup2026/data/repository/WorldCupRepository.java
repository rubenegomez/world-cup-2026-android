package com.example.worldcup2026.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J,\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b2\u0006\u0010\r\u001a\u00020\tH\u0002J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J(\u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u0011H\u0002J\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000bH\u0086@\u00a2\u0006\u0002\u0010\u0018J\u0014\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\t0\u000bH\u0086@\u00a2\u0006\u0002\u0010\u0018J\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\u000bH\u0086@\u00a2\u0006\u0002\u0010\u0018JB\u0010\u001c\u001a\u00020\u00062\u0006\u0010\u001d\u001a\u00020\u00142\b\u0010\u001e\u001a\u0004\u0018\u00010\u00142\b\u0010\u001f\u001a\u0004\u0018\u00010\u00142\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\u00142\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u0014H\u0086@\u00a2\u0006\u0002\u0010\"R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/example/worldcup2026/data/repository/WorldCupRepository;", "", "matchDao", "Lcom/example/worldcup2026/data/local/MatchDao;", "(Lcom/example/worldcup2026/data/local/MatchDao;)V", "addMatchWithPersistence", "", "targetList", "", "Lcom/example/worldcup2026/data/model/Match;", "savedEntities", "", "Lcom/example/worldcup2026/data/local/MatchEntity;", "baseMatch", "createPlaceholderTeam", "Lcom/example/worldcup2026/data/model/Team;", "name", "", "createTeam", "id", "", "flagCode", "group", "getAllTeams", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMatches", "getMockGroups", "Lcom/example/worldcup2026/data/model/Group;", "saveMatchScore", "matchId", "homeScore", "awayScore", "homePenalties", "awayPenalties", "(ILjava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class WorldCupRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.worldcup2026.data.local.MatchDao matchDao = null;
    
    public WorldCupRepository(@org.jetbrains.annotations.NotNull()
    com.example.worldcup2026.data.local.MatchDao matchDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getMockGroups(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.worldcup2026.data.model.Group>> $completion) {
        return null;
    }
    
    private final com.example.worldcup2026.data.model.Team createTeam(int id, java.lang.String name, java.lang.String flagCode, java.lang.String group) {
        return null;
    }
    
    private final com.example.worldcup2026.data.model.Team createPlaceholderTeam(java.lang.String name) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getMatches(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.worldcup2026.data.model.Match>> $completion) {
        return null;
    }
    
    private final void addMatchWithPersistence(java.util.List<com.example.worldcup2026.data.model.Match> targetList, java.util.List<com.example.worldcup2026.data.local.MatchEntity> savedEntities, com.example.worldcup2026.data.model.Match baseMatch) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveMatchScore(int matchId, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homeScore, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayScore, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homePenalties, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayPenalties, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAllTeams(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.worldcup2026.data.model.Team>> $completion) {
        return null;
    }
}