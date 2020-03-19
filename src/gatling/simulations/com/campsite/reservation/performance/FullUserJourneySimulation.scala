package com.campsite.reservation.performance

import java.time.LocalDate
import java.util.stream.IntStream

import io.gatling.http.protocol.HttpProtocolBuilder.toHttpProtocol
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.util.concurrent.atomic.AtomicInteger

class FullUserJourneySimulation extends Simulation {

  val userCount = Integer.getInteger("userCount", 7)

  val count: AtomicInteger = new AtomicInteger(0)
  def computeNext(): Int = { count.addAndGet(4).intValue() }
  val feeder = Iterator.continually(Map("num" -> computeNext()))

  val httpProtocol = http
    .baseUrl("http://localhost:8080")

  object FindAvailability {
    val run = exec(http("FindAvailability")
          .get("/reservations")
              .queryParam("from", "${from}")
              .queryParam("to", "${to}")
              .check(status is 200)
      )
  }

  object MakeReservation {
    val run = exec(http("MakeReservation")
        .post("/reservations")
            .headers(Map("Accept" -> "application/json", "Content-Type" -> "application/json"))
            .body(StringBody("""
                {
                	"email": "john.doe@gmail.com",
                	"fullName": "John Doe",
                	"dateRange": {
                		"from": "${from}",
                		"to": "${to}"
                	}
                }
                """))
        .check(header("Location").saveAs("bookingLocation"))
        .check(status is 201)
    )
  }

  object ModifyReservation {
    val run = exec(http("ModifyReservation")
        .put("${bookingLocation}")
            .headers(Map("Accept" -> "application/json", "Content-Type" -> "application/json"))
            .body(StringBody("""
                {
              		"from": "${newFrom}",
              		"to": "${newTo}"
                }
                """))
        .check(
             status is 200,
             jsonPath("$.dateRange.from") is "${newFrom}",
             jsonPath("$.dateRange.to") is "${newTo}"
        )
    )
  }

  object GetReservationInfo {
    val run = exec(http("GetReservationInfo")
        .get("${bookingLocation}")
             .headers(Map("Accept" -> "application/json", "Content-Type" -> "application/json"))
        .check(
             status is 200,
             jsonPath("$.email") is "john.doe@gmail.com",
             jsonPath("$.fullName") is "John Doe",
             jsonPath("$.dateRange.from") is "${newFrom}",
             jsonPath("$.dateRange.to") is "${newTo}"
        )
    )
  }

  object CancelReservation {
    val run = exec(http("CancelReservation")
        .delete("${bookingLocation}")
        .check(status is 200)
    )
  }
  
  object CheckReservationIsCancelled {
    val run = exec(http("CheckReservationIsCancelled")
        .get("${bookingLocation}")
        .check(status is 404)
    )
  }  

  val scn = scenario("Full User Journey")
    .feed(feeder)
    .exec(session => {
        val num = session("num").as[Int]
        val tomorrow = LocalDate.now().plusDays(1).plusDays(num)
        val from = tomorrow
        val to = tomorrow.plusDays(2)
        val newFrom = from.plusDays(1)
        val newTo = to.plusDays(1)
        session
            .set("from", from)
            .set("to", to)
            .set("newFrom", newFrom)
            .set("newTo",newTo)
    })
    .exec(FindAvailability.run, 
        MakeReservation.run,
        ModifyReservation.run, 
        GetReservationInfo.run,
        CancelReservation.run,
        CheckReservationIsCancelled.run
    )

  setUp(scn.inject(atOnceUsers(userCount)).protocols(httpProtocol))
}