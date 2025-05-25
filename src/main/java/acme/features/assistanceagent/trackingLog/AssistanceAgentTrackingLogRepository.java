
package acme.features.assistanceagent.trackingLog;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.claims.Claim;
import acme.entities.trackingLogs.TrackingLog;

@Repository
public interface AssistanceAgentTrackingLogRepository extends AbstractRepository {

	@Query("SELECT c FROM Claim c WHERE c.id = :claimId")
	Claim findClaimById(int claimId);

	@Query("SELECT tl FROM TrackingLog tl WHERE tl.claim.id = :claimId")
	Collection<TrackingLog> findAllTrackingLogsByClaimId(int claimId);

	@Query("SELECT tl FROM TrackingLog tl WHERE tl.id = :trackingLogId")
	TrackingLog findTrackingLogById(int trackingLogId);

	@Query("select max(t.resolutionPercentage) from TrackingLog t where t.claim.id = :claimId and t.id != :id")
	Double findMaxResolutionPercentageByClaimId(int id, int claimId);

	@Query("SELECT COUNT(t) FROM TrackingLog t WHERE t.claim.id = :claimId AND t.resolutionPercentage = 100.00 AND t.publish = true")
	Long countTrackingLogsForExceptionalCase(int claimId);

	@Query("select max(t.lastUpdateMoment) from TrackingLog t where t.claim.id = :claimId and t.id != :id")
	Date findMaxLastUpdateMomentByClaimId(int id, int claimId);

	@Query("SELECT COUNT(t) FROM TrackingLog t WHERE t.claim.id = :claimId AND t.publish = false")
	Long countUnpublishedLogsByClaimId(int claimId);

	@Query("SELECT COUNT(t) FROM TrackingLog t WHERE t.claim.id = :claimId")
	Long countAllByClaimId(int claimId);

	@Query("SELECT MAX(t.resolutionPercentage) FROM TrackingLog t WHERE t.claim.id = :claimId AND t.publish = true")
	Double findMaxPublishedResolutionPercentageByClaimId(int claimId);

	@Query("SELECT tl FROM TrackingLog tl WHERE tl.claim.id = :claimId AND tl.publish = true ORDER BY tl.lastUpdateMoment ASC")
	List<TrackingLog> findPublishedTrackingLogsByClaimId(int claimId);

}
