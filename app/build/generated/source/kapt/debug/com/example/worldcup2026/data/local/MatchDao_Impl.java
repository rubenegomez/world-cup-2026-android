package com.example.worldcup2026.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MatchDao_Impl implements MatchDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MatchEntity> __insertionAdapterOfMatchEntity;

  public MatchDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMatchEntity = new EntityInsertionAdapter<MatchEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `matches` (`id`,`homeScore`,`awayScore`,`homePenalties`,`awayPenalties`,`status`,`predictedWinner`,`predictedHomeScore`,`predictedAwayScore`,`homePossession`,`awayPossession`,`homeShots`,`awayShots`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MatchEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getHomeScore() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getHomeScore());
        }
        if (entity.getAwayScore() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getAwayScore());
        }
        if (entity.getHomePenalties() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getHomePenalties());
        }
        if (entity.getAwayPenalties() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getAwayPenalties());
        }
        if (entity.getStatus() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStatus());
        }
        if (entity.getPredictedWinner() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPredictedWinner());
        }
        if (entity.getPredictedHomeScore() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getPredictedHomeScore());
        }
        if (entity.getPredictedAwayScore() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getPredictedAwayScore());
        }
        if (entity.getHomePossession() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getHomePossession());
        }
        if (entity.getAwayPossession() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getAwayPossession());
        }
        if (entity.getHomeShots() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getHomeShots());
        }
        if (entity.getAwayShots() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getAwayShots());
        }
      }
    };
  }

  @Override
  public Object insertMatch(final MatchEntity match, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMatchEntity.insert(match);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<MatchEntity> matches,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMatchEntity.insert(matches);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MatchEntity>> getAllMatches() {
    final String _sql = "SELECT * FROM matches";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"matches"}, new Callable<List<MatchEntity>>() {
      @Override
      @NonNull
      public List<MatchEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHomeScore = CursorUtil.getColumnIndexOrThrow(_cursor, "homeScore");
          final int _cursorIndexOfAwayScore = CursorUtil.getColumnIndexOrThrow(_cursor, "awayScore");
          final int _cursorIndexOfHomePenalties = CursorUtil.getColumnIndexOrThrow(_cursor, "homePenalties");
          final int _cursorIndexOfAwayPenalties = CursorUtil.getColumnIndexOrThrow(_cursor, "awayPenalties");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPredictedWinner = CursorUtil.getColumnIndexOrThrow(_cursor, "predictedWinner");
          final int _cursorIndexOfPredictedHomeScore = CursorUtil.getColumnIndexOrThrow(_cursor, "predictedHomeScore");
          final int _cursorIndexOfPredictedAwayScore = CursorUtil.getColumnIndexOrThrow(_cursor, "predictedAwayScore");
          final int _cursorIndexOfHomePossession = CursorUtil.getColumnIndexOrThrow(_cursor, "homePossession");
          final int _cursorIndexOfAwayPossession = CursorUtil.getColumnIndexOrThrow(_cursor, "awayPossession");
          final int _cursorIndexOfHomeShots = CursorUtil.getColumnIndexOrThrow(_cursor, "homeShots");
          final int _cursorIndexOfAwayShots = CursorUtil.getColumnIndexOrThrow(_cursor, "awayShots");
          final List<MatchEntity> _result = new ArrayList<MatchEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MatchEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final Integer _tmpHomeScore;
            if (_cursor.isNull(_cursorIndexOfHomeScore)) {
              _tmpHomeScore = null;
            } else {
              _tmpHomeScore = _cursor.getInt(_cursorIndexOfHomeScore);
            }
            final Integer _tmpAwayScore;
            if (_cursor.isNull(_cursorIndexOfAwayScore)) {
              _tmpAwayScore = null;
            } else {
              _tmpAwayScore = _cursor.getInt(_cursorIndexOfAwayScore);
            }
            final Integer _tmpHomePenalties;
            if (_cursor.isNull(_cursorIndexOfHomePenalties)) {
              _tmpHomePenalties = null;
            } else {
              _tmpHomePenalties = _cursor.getInt(_cursorIndexOfHomePenalties);
            }
            final Integer _tmpAwayPenalties;
            if (_cursor.isNull(_cursorIndexOfAwayPenalties)) {
              _tmpAwayPenalties = null;
            } else {
              _tmpAwayPenalties = _cursor.getInt(_cursorIndexOfAwayPenalties);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpPredictedWinner;
            if (_cursor.isNull(_cursorIndexOfPredictedWinner)) {
              _tmpPredictedWinner = null;
            } else {
              _tmpPredictedWinner = _cursor.getString(_cursorIndexOfPredictedWinner);
            }
            final Integer _tmpPredictedHomeScore;
            if (_cursor.isNull(_cursorIndexOfPredictedHomeScore)) {
              _tmpPredictedHomeScore = null;
            } else {
              _tmpPredictedHomeScore = _cursor.getInt(_cursorIndexOfPredictedHomeScore);
            }
            final Integer _tmpPredictedAwayScore;
            if (_cursor.isNull(_cursorIndexOfPredictedAwayScore)) {
              _tmpPredictedAwayScore = null;
            } else {
              _tmpPredictedAwayScore = _cursor.getInt(_cursorIndexOfPredictedAwayScore);
            }
            final Integer _tmpHomePossession;
            if (_cursor.isNull(_cursorIndexOfHomePossession)) {
              _tmpHomePossession = null;
            } else {
              _tmpHomePossession = _cursor.getInt(_cursorIndexOfHomePossession);
            }
            final Integer _tmpAwayPossession;
            if (_cursor.isNull(_cursorIndexOfAwayPossession)) {
              _tmpAwayPossession = null;
            } else {
              _tmpAwayPossession = _cursor.getInt(_cursorIndexOfAwayPossession);
            }
            final Integer _tmpHomeShots;
            if (_cursor.isNull(_cursorIndexOfHomeShots)) {
              _tmpHomeShots = null;
            } else {
              _tmpHomeShots = _cursor.getInt(_cursorIndexOfHomeShots);
            }
            final Integer _tmpAwayShots;
            if (_cursor.isNull(_cursorIndexOfAwayShots)) {
              _tmpAwayShots = null;
            } else {
              _tmpAwayShots = _cursor.getInt(_cursorIndexOfAwayShots);
            }
            _item = new MatchEntity(_tmpId,_tmpHomeScore,_tmpAwayScore,_tmpHomePenalties,_tmpAwayPenalties,_tmpStatus,_tmpPredictedWinner,_tmpPredictedHomeScore,_tmpPredictedAwayScore,_tmpHomePossession,_tmpAwayPossession,_tmpHomeShots,_tmpAwayShots);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMatchById(final int matchId,
      final Continuation<? super MatchEntity> $completion) {
    final String _sql = "SELECT * FROM matches WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, matchId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MatchEntity>() {
      @Override
      @Nullable
      public MatchEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHomeScore = CursorUtil.getColumnIndexOrThrow(_cursor, "homeScore");
          final int _cursorIndexOfAwayScore = CursorUtil.getColumnIndexOrThrow(_cursor, "awayScore");
          final int _cursorIndexOfHomePenalties = CursorUtil.getColumnIndexOrThrow(_cursor, "homePenalties");
          final int _cursorIndexOfAwayPenalties = CursorUtil.getColumnIndexOrThrow(_cursor, "awayPenalties");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPredictedWinner = CursorUtil.getColumnIndexOrThrow(_cursor, "predictedWinner");
          final int _cursorIndexOfPredictedHomeScore = CursorUtil.getColumnIndexOrThrow(_cursor, "predictedHomeScore");
          final int _cursorIndexOfPredictedAwayScore = CursorUtil.getColumnIndexOrThrow(_cursor, "predictedAwayScore");
          final int _cursorIndexOfHomePossession = CursorUtil.getColumnIndexOrThrow(_cursor, "homePossession");
          final int _cursorIndexOfAwayPossession = CursorUtil.getColumnIndexOrThrow(_cursor, "awayPossession");
          final int _cursorIndexOfHomeShots = CursorUtil.getColumnIndexOrThrow(_cursor, "homeShots");
          final int _cursorIndexOfAwayShots = CursorUtil.getColumnIndexOrThrow(_cursor, "awayShots");
          final MatchEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final Integer _tmpHomeScore;
            if (_cursor.isNull(_cursorIndexOfHomeScore)) {
              _tmpHomeScore = null;
            } else {
              _tmpHomeScore = _cursor.getInt(_cursorIndexOfHomeScore);
            }
            final Integer _tmpAwayScore;
            if (_cursor.isNull(_cursorIndexOfAwayScore)) {
              _tmpAwayScore = null;
            } else {
              _tmpAwayScore = _cursor.getInt(_cursorIndexOfAwayScore);
            }
            final Integer _tmpHomePenalties;
            if (_cursor.isNull(_cursorIndexOfHomePenalties)) {
              _tmpHomePenalties = null;
            } else {
              _tmpHomePenalties = _cursor.getInt(_cursorIndexOfHomePenalties);
            }
            final Integer _tmpAwayPenalties;
            if (_cursor.isNull(_cursorIndexOfAwayPenalties)) {
              _tmpAwayPenalties = null;
            } else {
              _tmpAwayPenalties = _cursor.getInt(_cursorIndexOfAwayPenalties);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpPredictedWinner;
            if (_cursor.isNull(_cursorIndexOfPredictedWinner)) {
              _tmpPredictedWinner = null;
            } else {
              _tmpPredictedWinner = _cursor.getString(_cursorIndexOfPredictedWinner);
            }
            final Integer _tmpPredictedHomeScore;
            if (_cursor.isNull(_cursorIndexOfPredictedHomeScore)) {
              _tmpPredictedHomeScore = null;
            } else {
              _tmpPredictedHomeScore = _cursor.getInt(_cursorIndexOfPredictedHomeScore);
            }
            final Integer _tmpPredictedAwayScore;
            if (_cursor.isNull(_cursorIndexOfPredictedAwayScore)) {
              _tmpPredictedAwayScore = null;
            } else {
              _tmpPredictedAwayScore = _cursor.getInt(_cursorIndexOfPredictedAwayScore);
            }
            final Integer _tmpHomePossession;
            if (_cursor.isNull(_cursorIndexOfHomePossession)) {
              _tmpHomePossession = null;
            } else {
              _tmpHomePossession = _cursor.getInt(_cursorIndexOfHomePossession);
            }
            final Integer _tmpAwayPossession;
            if (_cursor.isNull(_cursorIndexOfAwayPossession)) {
              _tmpAwayPossession = null;
            } else {
              _tmpAwayPossession = _cursor.getInt(_cursorIndexOfAwayPossession);
            }
            final Integer _tmpHomeShots;
            if (_cursor.isNull(_cursorIndexOfHomeShots)) {
              _tmpHomeShots = null;
            } else {
              _tmpHomeShots = _cursor.getInt(_cursorIndexOfHomeShots);
            }
            final Integer _tmpAwayShots;
            if (_cursor.isNull(_cursorIndexOfAwayShots)) {
              _tmpAwayShots = null;
            } else {
              _tmpAwayShots = _cursor.getInt(_cursorIndexOfAwayShots);
            }
            _result = new MatchEntity(_tmpId,_tmpHomeScore,_tmpAwayScore,_tmpHomePenalties,_tmpAwayPenalties,_tmpStatus,_tmpPredictedWinner,_tmpPredictedHomeScore,_tmpPredictedAwayScore,_tmpHomePossession,_tmpAwayPossession,_tmpHomeShots,_tmpAwayShots);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
