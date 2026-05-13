package com.example.worldcup2026.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010 \n\u0002\b0\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B\u00d1\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u000b\u001a\u00020\f\u0012\u0006\u0010\r\u001a\u00020\f\u0012\b\b\u0002\u0010\u000e\u001a\u00020\f\u0012\b\b\u0002\u0010\u000f\u001a\u00020\f\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\f\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0003\u0012\u000e\b\u0002\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\f0\u0018\u00a2\u0006\u0002\u0010\u0019J\t\u00103\u001a\u00020\u0003H\u00c6\u0003J\t\u00104\u001a\u00020\fH\u00c6\u0003J\t\u00105\u001a\u00020\fH\u00c6\u0003J\u000b\u00106\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\u0010\u00107\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001bJ\u0010\u00108\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001bJ\u0010\u00109\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001bJ\u0010\u0010:\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001bJ\u0010\u0010;\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001bJ\u0010\u0010<\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001bJ\u000f\u0010=\u001a\b\u0012\u0004\u0012\u00020\f0\u0018H\u00c6\u0003J\t\u0010>\u001a\u00020\u0005H\u00c6\u0003J\t\u0010?\u001a\u00020\u0005H\u00c6\u0003J\u0010\u0010@\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001bJ\u0010\u0010A\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001bJ\u0010\u0010B\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001bJ\u0010\u0010C\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001bJ\t\u0010D\u001a\u00020\fH\u00c6\u0003J\t\u0010E\u001a\u00020\fH\u00c6\u0003J\u00e8\u0001\u0010F\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\f2\b\b\u0002\u0010\u000e\u001a\u00020\f2\b\b\u0002\u0010\u000f\u001a\u00020\f2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00032\u000e\b\u0002\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\f0\u0018H\u00c6\u0001\u00a2\u0006\u0002\u0010GJ\u0013\u0010H\u001a\u00020I2\b\u0010J\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010K\u001a\u00020\u0003H\u00d6\u0001J\t\u0010L\u001a\u00020\fH\u00d6\u0001R\u0015\u0010\n\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u001c\u001a\u0004\b\u001a\u0010\u001bR\u0015\u0010\u0014\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u001c\u001a\u0004\b\u001d\u0010\u001bR\u0015\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u001c\u001a\u0004\b\u001e\u0010\u001bR\u0015\u0010\u0016\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u001c\u001a\u0004\b\u001f\u0010\u001bR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0011\u0010\u000f\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010#R\u0015\u0010\t\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u001c\u001a\u0004\b%\u0010\u001bR\u0015\u0010\u0013\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u001c\u001a\u0004\b&\u0010\u001bR\u0015\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u001c\u001a\u0004\b\'\u0010\u001bR\u0015\u0010\u0015\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u001c\u001a\u0004\b(\u0010\u001bR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010!R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010+R\u0015\u0010\u0012\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u001c\u001a\u0004\b,\u0010\u001bR\u0015\u0010\u0011\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u001c\u001a\u0004\b-\u0010\u001bR\u0013\u0010\u0010\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010#R\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\f0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u00100R\u0011\u0010\u000e\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010#R\u0011\u0010\r\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010#\u00a8\u0006M"}, d2 = {"Lcom/example/worldcup2026/data/model/Match;", "", "id", "", "homeTeam", "Lcom/example/worldcup2026/data/model/Team;", "awayTeam", "homeScore", "awayScore", "homePenalties", "awayPenalties", "date", "", "status", "stadium", "city", "predictedWinner", "predictedHomeScore", "predictedAwayScore", "homePossession", "awayPossession", "homeShots", "awayShots", "scorers", "", "(ILcom/example/worldcup2026/data/model/Team;Lcom/example/worldcup2026/data/model/Team;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/util/List;)V", "getAwayPenalties", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getAwayPossession", "getAwayScore", "getAwayShots", "getAwayTeam", "()Lcom/example/worldcup2026/data/model/Team;", "getCity", "()Ljava/lang/String;", "getDate", "getHomePenalties", "getHomePossession", "getHomeScore", "getHomeShots", "getHomeTeam", "getId", "()I", "getPredictedAwayScore", "getPredictedHomeScore", "getPredictedWinner", "getScorers", "()Ljava/util/List;", "getStadium", "getStatus", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(ILcom/example/worldcup2026/data/model/Team;Lcom/example/worldcup2026/data/model/Team;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/util/List;)Lcom/example/worldcup2026/data/model/Match;", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class Match {
    private final int id = 0;
    @org.jetbrains.annotations.NotNull()
    private final com.example.worldcup2026.data.model.Team homeTeam = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.worldcup2026.data.model.Team awayTeam = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer homeScore = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer awayScore = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer homePenalties = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer awayPenalties = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String date = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String status = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String stadium = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String city = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String predictedWinner = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer predictedHomeScore = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer predictedAwayScore = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer homePossession = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer awayPossession = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer homeShots = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer awayShots = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> scorers = null;
    
    public Match(int id, @org.jetbrains.annotations.NotNull()
    com.example.worldcup2026.data.model.Team homeTeam, @org.jetbrains.annotations.NotNull()
    com.example.worldcup2026.data.model.Team awayTeam, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homeScore, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayScore, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homePenalties, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayPenalties, @org.jetbrains.annotations.NotNull()
    java.lang.String date, @org.jetbrains.annotations.NotNull()
    java.lang.String status, @org.jetbrains.annotations.NotNull()
    java.lang.String stadium, @org.jetbrains.annotations.NotNull()
    java.lang.String city, @org.jetbrains.annotations.Nullable()
    java.lang.String predictedWinner, @org.jetbrains.annotations.Nullable()
    java.lang.Integer predictedHomeScore, @org.jetbrains.annotations.Nullable()
    java.lang.Integer predictedAwayScore, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homePossession, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayPossession, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homeShots, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayShots, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> scorers) {
        super();
    }
    
    public final int getId() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.worldcup2026.data.model.Team getHomeTeam() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.worldcup2026.data.model.Team getAwayTeam() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getHomeScore() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getAwayScore() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getHomePenalties() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getAwayPenalties() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStadium() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCity() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPredictedWinner() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getPredictedHomeScore() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getPredictedAwayScore() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getHomePossession() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getAwayPossession() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getHomeShots() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getAwayShots() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getScorers() {
        return null;
    }
    
    public final int component1() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component16() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component17() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component18() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component19() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.worldcup2026.data.model.Team component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.worldcup2026.data.model.Team component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.worldcup2026.data.model.Match copy(int id, @org.jetbrains.annotations.NotNull()
    com.example.worldcup2026.data.model.Team homeTeam, @org.jetbrains.annotations.NotNull()
    com.example.worldcup2026.data.model.Team awayTeam, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homeScore, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayScore, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homePenalties, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayPenalties, @org.jetbrains.annotations.NotNull()
    java.lang.String date, @org.jetbrains.annotations.NotNull()
    java.lang.String status, @org.jetbrains.annotations.NotNull()
    java.lang.String stadium, @org.jetbrains.annotations.NotNull()
    java.lang.String city, @org.jetbrains.annotations.Nullable()
    java.lang.String predictedWinner, @org.jetbrains.annotations.Nullable()
    java.lang.Integer predictedHomeScore, @org.jetbrains.annotations.Nullable()
    java.lang.Integer predictedAwayScore, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homePossession, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayPossession, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homeShots, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayShots, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> scorers) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}