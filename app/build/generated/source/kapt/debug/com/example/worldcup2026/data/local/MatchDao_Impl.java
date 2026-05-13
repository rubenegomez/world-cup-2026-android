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
        return "INSERT OR REPLACE INTO `matches` (`id`,`homeScore`,`awayScore`,`homePenalties`,`awayPenalties`,`status`) VALUES (?,?,?,?,?,?)";
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
            _item = new MatchEntity(_tmpId,_tmpHomeScore,_tmpAwayScore,_tmpHomePenalties,_tmpAwayPenalties,_tmpStatus);
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
            _result = new MatchEntity(_tmpId,_tmpHomeScore,_tmpAwayScore,_tmpHomePenalties,_tmpAwayPenalties,_tmpStatus);
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
