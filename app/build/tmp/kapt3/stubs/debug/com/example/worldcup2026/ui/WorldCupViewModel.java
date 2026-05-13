package com.example.worldcup2026.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u000e\u001a\u00020\u000fH\u0002J\'\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u00122\b\u0010\u0014\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\u0002\u0010\u0015J\'\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u00122\b\u0010\u0017\u001a\u0004\u0018\u00010\u00122\b\u0010\u0018\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\u0002\u0010\u0015R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00070\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0019"}, d2 = {"Lcom/example/worldcup2026/ui/WorldCupViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_uiState", "Landroidx/compose/runtime/MutableState;", "Lcom/example/worldcup2026/ui/WorldCupUiState;", "repository", "Lcom/example/worldcup2026/data/repository/WorldCupRepository;", "uiState", "Landroidx/compose/runtime/State;", "getUiState", "()Landroidx/compose/runtime/State;", "loadData", "", "updateMatchPenalties", "matchId", "", "homePenalties", "awayPenalties", "(ILjava/lang/Integer;Ljava/lang/Integer;)V", "updateMatchScore", "homeScore", "awayScore", "app_debug"})
public final class WorldCupViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.worldcup2026.data.repository.WorldCupRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState<com.example.worldcup2026.ui.WorldCupUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.State<com.example.worldcup2026.ui.WorldCupUiState> uiState = null;
    
    public WorldCupViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.compose.runtime.State<com.example.worldcup2026.ui.WorldCupUiState> getUiState() {
        return null;
    }
    
    private final void loadData() {
    }
    
    public final void updateMatchScore(int matchId, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homeScore, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayScore) {
    }
    
    public final void updateMatchPenalties(int matchId, @org.jetbrains.annotations.Nullable()
    java.lang.Integer homePenalties, @org.jetbrains.annotations.Nullable()
    java.lang.Integer awayPenalties) {
    }
}