/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.xieoricommoncomponentfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.{SessionCache, UserAnswersCache}
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.AddressLookupConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthAction
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.AddressResultsFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ContactAddressLookup
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{AddressLookupFailure, AddressLookupSuccess}
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.contact_address_results

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactAddressResultController @Inject() (
  authAction: AuthAction,
  mcc: MessagesControllerComponents,
  addressLookupConnector: AddressLookupConnector,
  sessionCache: SessionCache,
  userAnswersCache: UserAnswersCache,
  contactAddressResultsView: contact_address_results
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        displayPage()
    }

  private def displayPage()(implicit request: Request[AnyContent]): Future[Result] =
    sessionCache.contactAddressParams.flatMap {
      case Some(addressLookupParams) if !addressLookupParams.isEmpty() =>
        addressLookupConnector.lookup(addressLookupParams.postcode, addressLookupParams.line1).flatMap {
          case AddressLookupSuccess(addresses) if addresses.nonEmpty && addresses.forall(_.nonEmpty) =>
            Future.successful(
              Ok(
                contactAddressResultsView(
                  AddressResultsFormProvider.form(addresses.map(_.dropDownView)),
                  addressLookupParams,
                  addresses
                )
              )
            )
          case AddressLookupSuccess(_) if addressLookupParams.line1.exists(_.nonEmpty) =>
            viewForAddressWithoutLine1Case(addressLookupParams)
          case AddressLookupSuccess(_) => Future.successful(displayNoResultsPage())
          case AddressLookupFailure    => throw AddressLookupException
        }.recoverWith {
          case _ => Future.successful(displayErrorPage())
        }
      case _ =>
        Future.successful(
          Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.PBEAddressLookupController.onPageLoad())
        )
    }

  private def viewForAddressWithoutLine1Case(
    addressLookupParams: ContactAddressLookup
  )(implicit request: Request[AnyContent], hc: HeaderCarrier): Future[Result] = {
    val addressLookupParamsWithoutLine1 = ContactAddressLookup(addressLookupParams.postcode, None)

    addressLookupConnector.lookup(addressLookupParamsWithoutLine1.postcode, None).flatMap {
      case AddressLookupSuccess(addresses) if addresses.nonEmpty && addresses.forall(_.nonEmpty) =>
        sessionCache.saveContactAddressParams(addressLookupParamsWithoutLine1).map { _ =>
          Ok(
            contactAddressResultsView(
              AddressResultsFormProvider.form(addresses.map(_.dropDownView)),
              addressLookupParams,
              addresses
            )
          )
        }
      case AddressLookupSuccess(_) => Future.successful(displayNoResultsPage())
      case AddressLookupFailure    => throw AddressLookupException
    }
  }

  def submit(): Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction { implicit request => _: LoggedInUserWithEnrolments =>
      sessionCache.contactAddressParams.flatMap {
        case Some(addressLookupParams) =>
          addressLookupConnector.lookup(addressLookupParams.postcode, addressLookupParams.line1).flatMap {
            case AddressLookupSuccess(addresses) if addresses.nonEmpty && addresses.forall(_.nonEmpty) =>
              val addressesMap  = addresses.map(address => address.dropDownView -> address).toMap
              val addressesView = addressesMap.keys.toSeq

              AddressResultsFormProvider.form(addressesView).bindFromRequest.fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(contactAddressResultsView(formWithErrors, addressLookupParams, addresses))
                  ),
                validAnswer => {
                  val address = addressesMap(validAnswer.address).toAddressViewModel
                  userAnswersCache.cacheContactAddressDetails(address).map { _ =>
                    Redirect(
                      uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactAddressLookupController.onPageLoad()
                    )
                  }
                }
              )
            case AddressLookupSuccess(_) => Future.successful(displayNoResultsPage())
            case AddressLookupFailure    => throw AddressLookupException
          }.recoverWith {
            case _ => Future.successful(displayErrorPage())
          }
        case _ =>
          Future.successful(
            Redirect(
              uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactAddressLookupController.onPageLoad()
            )
          )
      }
    }

  def displayErrorPage(): Result =
    Redirect(
      uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.AddressLookupErrorController.displayContactAddressErrorPage()
    )

  def displayNoResultsPage(): Result =
    Redirect(
      uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.AddressLookupErrorController.displayNoContactAddressResultsPage()
    )

}
