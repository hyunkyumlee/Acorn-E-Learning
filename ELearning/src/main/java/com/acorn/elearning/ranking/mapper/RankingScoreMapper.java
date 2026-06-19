package com.acorn.elearning.ranking.mapper;

import com.acorn.elearning.ranking.model.RankingScore;
import java.util.List;
import java.util.Optional;

public interface RankingScoreMapper {
    Optional<RankingScore> findById(Long id);
    List<RankingScore> findAll();
    int insert(RankingScore model);
    int update(RankingScore model);
}
