
package acme.entities.claims;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.trackingLogs.TrackingLog;

@Repository
public interface ClaimRepository extends AbstractRepository {

	//	@Query("SELECT t FROM TrackingLog t WHERE t.claim.id = :claimId ORDER BY t.lastUpdateMoment DESC")
	//	Collection<TrackingLog> findLastTrackingLogByClaimId(@Param("claimId") int claimId);

	@Query("SELECT t FROM TrackingLog t WHERE t.claim.id = :claimId AND t.publish = true ORDER BY t.resolutionPercentage DESC, t.lastUpdateMoment DESC")
	Collection<TrackingLog> findLastTrackingLogByClaimId(int claimId);

}
