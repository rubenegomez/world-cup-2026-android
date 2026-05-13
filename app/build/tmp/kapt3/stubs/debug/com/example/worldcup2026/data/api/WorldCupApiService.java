package com.example.worldcup2026.data.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J(\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0003\u0010\u0005\u001a\u00020\u00062\b\b\u0003\u0010\u0007\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\bJ(\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u00032\b\b\u0003\u0010\u0005\u001a\u00020\u00062\b\b\u0003\u0010\u0007\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\b\u00a8\u0006\u000b"}, d2 = {"Lcom/example/worldcup2026/data/api/WorldCupApiService;", "", "getMatches", "", "Lcom/example/worldcup2026/data/model/Match;", "leagueId", "", "season", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTeams", "Lcom/example/worldcup2026/data/model/Team;", "app_debug"})
public abstract interface WorldCupApiService {
    
    @retrofit2.http.GET(value = "teams")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTeams(@retrofit2.http.Query(value = "league")
    int leagueId, @retrofit2.http.Query(value = "season")
    int season, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.worldcup2026.data.model.Team>> $completion);
    
    @retrofit2.http.GET(value = "fixtures")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMatches(@retrofit2.http.Query(value = "league")
    int leagueId, @retrofit2.http.Query(value = "season")
    int season, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.worldcup2026.data.model.Match>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}