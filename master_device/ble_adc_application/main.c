/* Copyright (c) 2014 Nordic Semiconductor. All Rights Reserved.
 *
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC
 * SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 *
 * Licensees are granted free, non-transferable use of the information. NO
 * WARRANTY of ANY KIND is provided. This heading must NOT be removed from
 * the file.
 *
 */
/** @example examples/ble_peripheral/ble_app_hrs/main.c
 *
 * @brief Heart Rate Service Sample Application main file.
 *
 * This file contains the source code for a sample application using the Heart Rate service
 * (and also Battery and Device Information services). This application uses the
 * @ref srvlib_conn_params module.
 */

#include <stdint.h>
#include <string.h>
#include "nordic_common.h"
#include "nrf.h"
#include "app_error.h"
#include "ble.h"
#include "ble_hci.h"
#include "ble_srv_common.h"
#include "ble_advdata.h"
#include "ble_advertising.h"
#include "ble_bas.h"
#include "ble_hrs.h"
#include "ble_dis.h"
#include "ble_conn_params.h"
#include "ble_lbs.h"
#include "boards.h"
#include "sensorsim.h"
#include "softdevice_handler.h"
#include "app_timer.h"
#include "bsp.h"
#include "nrf_delay.h"
#include "bsp_btn_ble.h"
#include "peer_manager.h"
#include "fds.h"
#include "fstorage.h"
#include "nrf_ble_gatt.h"
#include "ble_conn_state.h"

#include "nrf_drv_saadc.h"
#include "nrf_drv_ppi.h"
#include "nrf_drv_timer.h"
#include "nrf_drv_gpiote.h"

#define NRF_LOG_MODULE_NAME "APP"

#include "nrf_log.h"
#include "nrf_log_ctrl.h"

#define IS_SRVC_CHANGED_CHARACT_PRESENT  1                                           /**< Include or not the service_changed characteristic. if not enabled, the server's database cannot be changed for the lifetime of the device*/



#define DEVICE_NAME                      "Rhaeos_5ch"                               /**< Name of device. Will be included in the advertising data. */
#define MANUFACTURER_NAME                "JohnRogersGroup"                       /**< Manufacturer. Will be passed to Device Information Service. */
#define APP_ADV_INTERVAL                 400                                         /**< The advertising interval (in units of 0.625 ms. This value corresponds to 187.5 ms). */
#define APP_ADV_TIMEOUT_IN_SECONDS       60                                         /**< The advertising timeout in units of seconds. */

#define APP_TIMER_PRESCALER              0                                           /**< Value of the RTC1 PRESCALER register. */
#define APP_TIMER_OP_QUEUE_SIZE          4                                           /**< Size of timer operation queues. */

#define MIN_CONN_INTERVAL                MSEC_TO_UNITS(10, UNIT_1_25_MS)            /**< Minimum acceptable connection interval (0.4 seconds). */
#define MAX_CONN_INTERVAL                MSEC_TO_UNITS(20, UNIT_1_25_MS)            /**< Maximum acceptable connection interval (0.65 second). */
#define SLAVE_LATENCY                    0                                           /**< Slave latency. */
#define CONN_SUP_TIMEOUT                 MSEC_TO_UNITS(4000, UNIT_10_MS)             /**< Connection supervisory timeout (4 seconds). */

#define FIRST_CONN_PARAMS_UPDATE_DELAY   APP_TIMER_TICKS(1000, APP_TIMER_PRESCALER)  /**< Time from initiating event (connect or start of notification) to first time sd_ble_gap_conn_param_update is called (5 seconds). */
#define NEXT_CONN_PARAMS_UPDATE_DELAY    APP_TIMER_TICKS(30000, APP_TIMER_PRESCALER) /**< Time between each call to sd_ble_gap_conn_param_update after the first call (30 seconds). */
#define MAX_CONN_PARAMS_UPDATE_COUNT     3                                           /**< Number of attempts before giving up the connection parameter negotiation. */

#define SEC_PARAM_BOND                   1                                           /**< Perform bonding. */
#define SEC_PARAM_MITM                   0                                           /**< Man In The Middle protection not required. */
#define SEC_PARAM_LESC                   0                                           /**< LE Secure Connections not enabled. */
#define SEC_PARAM_KEYPRESS               0                                           /**< Keypress notifications not enabled. */
#define SEC_PARAM_IO_CAPABILITIES        BLE_GAP_IO_CAPS_NONE                        /**< No I/O capabilities. */
#define SEC_PARAM_OOB                    0                                           /**< Out Of Band data not available. */
#define SEC_PARAM_MIN_KEY_SIZE           7                                           /**< Minimum encryption key size. */
#define SEC_PARAM_MAX_KEY_SIZE           16                                          /**< Maximum encryption key size. */

#define DEAD_BEEF                        0xDEADBEEF                                  /**< Value used as error code on stack dump, can be used to identify stack location on stack unwind. */

#define APP_FEATURE_NOT_SUPPORTED        BLE_GATT_STATUS_ATTERR_APP_BEGIN + 2        /**< Reply when unsupported features are requested. */

#define HEAT_EN 22 
#define NFC_EN 8

#define LEDBUTTON_LED_PIN_NO            BSP_LED_1_MASK


static uint16_t  m_conn_handle = BLE_CONN_HANDLE_INVALID; /**< Handle of the current connection. */
static ble_hrs_t m_hrs;                                   /**< Structure used to identify the heart rate service. */



volatile uint8_t state = 1;
static uint8_t avg_ble_data[20]; 
static uint8_t ble_counter = 0;
static uint8_t avg_ble_counter = 0;
 
static uint32_t temp_avg[5];  

static nrf_ble_gatt_t m_gatt;                             /**< Structure for gatt module*/

static ble_lbs_t m_lbs;

static const nrf_drv_timer_t m_timer = NRF_DRV_TIMER_INSTANCE(3);
static nrf_saadc_value_t     m_buffer_pool[2][4];
static nrf_ppi_channel_t     m_ppi_channel;

APP_TIMER_DEF(m_heart_rate_timer_id);                    


static ble_uuid_t m_adv_uuids[] = {{BLE_UUID_HEART_RATE_SERVICE, BLE_UUID_TYPE_BLE},
                                   {BLE_UUID_DEVICE_INFORMATION_SERVICE, BLE_UUID_TYPE_BLE}}; /**< Universally unique service identifiers. */

/**@brief Callback function for asserts in the SoftDevice.
 *
 * @details This function will be called in case of an assert in the SoftDevice.
 *
 * @warning This handler is an example only and does not fit a final product. You need to analyze
 *          how your product is supposed to react in case of Assert.
 * @warning On assert from the SoftDevice, the system can only recover on reset.
 *
 * @param[in] line_num   Line number of the failing ASSERT call.
 * @param[in] file_name  File name of the failing ASSERT call.
 */
void assert_nrf_callback(uint16_t line_num, const uint8_t * p_file_name)
{
    app_error_handler(DEAD_BEEF, line_num, p_file_name);
}

/**@brief Function for starting advertising.
 */
void advertising_start(void)
{
    ret_code_t err_code;

    err_code = ble_advertising_start(BLE_ADV_MODE_FAST);
    APP_ERROR_CHECK(err_code);
}



static void timers_init(void)
{

    // Initialize timer module.
    APP_TIMER_INIT(APP_TIMER_PRESCALER, APP_TIMER_OP_QUEUE_SIZE, false);

}


/**@brief Function for the GAP initialization.
 *
 * @details This function sets up all the necessary GAP (Generic Access Profile) parameters of the
 *          device including the device name, appearance, and the preferred connection parameters.
 */
static void gap_params_init(void)
{
    uint32_t                err_code;
    ble_gap_conn_params_t   gap_conn_params;
    ble_gap_conn_sec_mode_t sec_mode;

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&sec_mode);

    err_code = sd_ble_gap_device_name_set(&sec_mode,
                                          (const uint8_t *)DEVICE_NAME,
                                          strlen(DEVICE_NAME));
    APP_ERROR_CHECK(err_code);


    memset(&gap_conn_params, 0, sizeof(gap_conn_params));

    gap_conn_params.min_conn_interval = MIN_CONN_INTERVAL;
    gap_conn_params.max_conn_interval = MAX_CONN_INTERVAL;
    gap_conn_params.slave_latency     = SLAVE_LATENCY;
    gap_conn_params.conn_sup_timeout  = CONN_SUP_TIMEOUT;

    err_code = sd_ble_gap_ppcp_set(&gap_conn_params);
    APP_ERROR_CHECK(err_code);
}



/**@brief Function called by LED Button Service to toggle an LED when LED characteristic is written by BLE central.
 * Used for toggling gpio pin.
 * 
 */
void led_write_handler(ble_lbs_t * p_lbs, uint8_t led_state)
{
		if (led_state)
		{
				LEDS_ON(LEDBUTTON_LED_PIN_NO);
                nrf_drv_gpiote_out_set(HEAT_EN);            
                nrf_drv_gpiote_out_set(NFC_EN); 
		}
		else
		{
				LEDS_OFF(LEDBUTTON_LED_PIN_NO);
                nrf_drv_gpiote_out_clear(HEAT_EN); 
                nrf_drv_gpiote_out_clear(NFC_EN);
		}
} 

/**@brief Function for initializing services that will be used by the application.
 *
 * @details Initialize the Heart Rate, Battery and Device Information services.
 */
static void services_init(void)
{
    uint32_t       err_code;
    ble_hrs_init_t hrs_init;
    //ble_bas_init_t bas_init;
    ble_dis_init_t dis_init;
    uint8_t        body_sensor_location;

    // Initialize Heart Rate Service.
    body_sensor_location = BLE_HRS_BODY_SENSOR_LOCATION_FINGER;

    memset(&hrs_init, 0, sizeof(hrs_init));

    hrs_init.evt_handler                 = NULL;
    hrs_init.is_sensor_contact_supported = true;
    hrs_init.p_body_sensor_location      = &body_sensor_location;

    // Here the sec level for the Heart Rate Service can be changed/increased.
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&hrs_init.hrs_hrm_attr_md.cccd_write_perm);
    BLE_GAP_CONN_SEC_MODE_SET_NO_ACCESS(&hrs_init.hrs_hrm_attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_NO_ACCESS(&hrs_init.hrs_hrm_attr_md.write_perm);

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&hrs_init.hrs_bsl_attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_NO_ACCESS(&hrs_init.hrs_bsl_attr_md.write_perm);

    err_code = ble_hrs_init(&m_hrs, &hrs_init);
    APP_ERROR_CHECK(err_code);


    // Initialize Device Information Service.
    memset(&dis_init, 0, sizeof(dis_init));

    ble_srv_ascii_to_utf8(&dis_init.manufact_name_str, (char *)MANUFACTURER_NAME);

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&dis_init.dis_attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_NO_ACCESS(&dis_init.dis_attr_md.write_perm);

    err_code = ble_dis_init(&dis_init);
    APP_ERROR_CHECK(err_code);

    //led stuff
	ble_lbs_init_t init;

	init.led_write_handler = led_write_handler;

    memset(&m_lbs, 0, sizeof(m_lbs));
	err_code = ble_lbs_init(&m_lbs, &init);
	APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling the Connection Parameters Module.
 *
 * @details This function will be called for all events in the Connection Parameters Module which
 *          are passed to the application.
 *          @note All this function does is to disconnect. This could have been done by simply
 *                setting the disconnect_on_fail config parameter, but instead we use the event
 *                handler mechanism to demonstrate its use.
 *
 * @param[in] p_evt  Event received from the Connection Parameters Module.
 */
static void on_conn_params_evt(ble_conn_params_evt_t * p_evt)
{
    
}


/**@brief Function for handling a Connection Parameters error.
 *
 * @param[in] nrf_error  Error code containing information about what went wrong.
 */
static void conn_params_error_handler(uint32_t nrf_error)
{
    APP_ERROR_HANDLER(nrf_error);
}


/**@brief Function for initializing the Connection Parameters module.
 */
static void conn_params_init(void)
{
    uint32_t               err_code;
    ble_conn_params_init_t cp_init;

    memset(&cp_init, 0, sizeof(cp_init));

    cp_init.p_conn_params                  = NULL;
    cp_init.first_conn_params_update_delay = FIRST_CONN_PARAMS_UPDATE_DELAY;
    cp_init.next_conn_params_update_delay  = NEXT_CONN_PARAMS_UPDATE_DELAY;
    cp_init.max_conn_params_update_count   = MAX_CONN_PARAMS_UPDATE_COUNT;
    cp_init.start_on_notify_cccd_handle    = m_hrs.hrm_handles.cccd_handle;
    cp_init.disconnect_on_fail             = false;
    cp_init.evt_handler                    = on_conn_params_evt;
    cp_init.error_handler                  = conn_params_error_handler;

    err_code = ble_conn_params_init(&cp_init);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for putting the chip into sleep mode.
 *
 * @note This function will not return.
 */
static void sleep_mode_enter(void)
{
    
    uint32_t err_code;

    nrf_drv_gpiote_out_clear(HEAT_EN);
    nrf_drv_gpiote_out_clear(NFC_EN);
    err_code = bsp_btn_ble_sleep_mode_prepare();
    APP_ERROR_CHECK(err_code);
    
    err_code = sd_power_system_off();
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling advertising events.
 *
 * @details This function will be called for advertising events which are passed to the application.
 *
 * @param[in] ble_adv_evt  Advertising event.
 */
static void on_adv_evt(ble_adv_evt_t ble_adv_evt)
{
    
    switch (ble_adv_evt)
    {
        case BLE_ADV_EVT_FAST:
            #if (DEBUG == 1)
            NRF_LOG_INFO("Fast Advertising\r\n");
            #endif
            
            break;

        case BLE_ADV_EVT_IDLE:            
            sleep_mode_enter();
            break;

        default:
            break;
    }
}


void saadc_callback(nrf_drv_saadc_evt_t const * p_event)
{
    if (p_event->type == NRF_DRV_SAADC_EVT_DONE)
    {
        ret_code_t err_code;
        int i=0;
        int j=0;
        //here?
        err_code = nrf_drv_saadc_buffer_convert(p_event->data.done.p_buffer, 5);
        APP_ERROR_CHECK(err_code);

      
        uint16_t temp_ch[5];

        for(i=0;i<5;i++)
        {
            temp_ch[i]=(uint16_t)(p_event->data.done.p_buffer[i]);
            if(temp_ch[i]>0x0FFF){
                temp_ch[i]=0x0000;
            }            
            temp_avg[i]+=temp_ch[i];                      
        }
        ble_counter++;   

        if(ble_counter == 25){     
                        
            for(j=0;j<5;j++)
            {
                temp_avg[j]=(uint32_t)(temp_avg[j]/ble_counter);
                avg_ble_data[avg_ble_counter++] = (uint8_t)((temp_avg[j] & 0xFF));
                avg_ble_data[avg_ble_counter++] = (uint8_t)((temp_avg[j] >> 8));
                temp_avg[j]=0;
            }
            ble_counter = 0;

            if(avg_ble_counter == 20){ 
                err_code = ble_hrs_heart_rate_measurement_send(&m_hrs, avg_ble_data);
                avg_ble_counter = 0;
            }            
        }      
        
    }
}



void saadc_init(void)
{
    ret_code_t err_code;

    nrf_drv_saadc_config_t saadc_config = NRF_DRV_SAADC_DEFAULT_CONFIG;
    saadc_config.resolution = NRF_SAADC_RESOLUTION_12BIT;
        
    nrf_saadc_channel_config_t channel_0_config =
        NRF_DRV_SAADC_DEFAULT_CHANNEL_CONFIG_SE(NRF_SAADC_INPUT_AIN3); 
		channel_0_config.gain = NRF_SAADC_GAIN1_6;
		channel_0_config.reference = NRF_SAADC_REFERENCE_INTERNAL;
        channel_0_config.acq_time = NRF_SAADC_ACQTIME_40US;

    err_code = nrf_drv_saadc_channel_init(0, &channel_0_config);
    APP_ERROR_CHECK(err_code);   
    
    nrf_saadc_channel_config_t channel_1_config =
        NRF_DRV_SAADC_DEFAULT_CHANNEL_CONFIG_SE(NRF_SAADC_INPUT_AIN4); 
        channel_1_config.gain = NRF_SAADC_GAIN1_6;
        channel_1_config.reference = NRF_SAADC_REFERENCE_INTERNAL;
        channel_1_config.acq_time = NRF_SAADC_ACQTIME_40US;

    err_code = nrf_drv_saadc_channel_init(1, &channel_1_config);
    APP_ERROR_CHECK(err_code);       
    
    nrf_saadc_channel_config_t channel_2_config =
        NRF_DRV_SAADC_DEFAULT_CHANNEL_CONFIG_SE(NRF_SAADC_INPUT_AIN5); 
        channel_2_config.gain = NRF_SAADC_GAIN1_6;
        channel_2_config.reference = NRF_SAADC_REFERENCE_INTERNAL;
        channel_2_config.acq_time = NRF_SAADC_ACQTIME_40US;

    err_code = nrf_drv_saadc_channel_init(2, &channel_2_config);
    APP_ERROR_CHECK(err_code);   
    
    nrf_saadc_channel_config_t channel_3_config =
        NRF_DRV_SAADC_DEFAULT_CHANNEL_CONFIG_SE(NRF_SAADC_INPUT_AIN6); 
        channel_3_config.gain = NRF_SAADC_GAIN1_6;
        channel_3_config.reference = NRF_SAADC_REFERENCE_INTERNAL;
        channel_3_config.acq_time = NRF_SAADC_ACQTIME_40US;

    err_code = nrf_drv_saadc_channel_init(3, &channel_3_config);
    APP_ERROR_CHECK(err_code); 

    nrf_saadc_channel_config_t channel_4_config =
        NRF_DRV_SAADC_DEFAULT_CHANNEL_CONFIG_SE(NRF_SAADC_INPUT_AIN0); 
        channel_4_config.gain = NRF_SAADC_GAIN1_6;
        channel_4_config.reference = NRF_SAADC_REFERENCE_INTERNAL;
        channel_4_config.acq_time = NRF_SAADC_ACQTIME_40US;

    err_code = nrf_drv_saadc_channel_init(4, &channel_4_config);
    APP_ERROR_CHECK(err_code);     

    err_code = nrf_drv_saadc_init(&saadc_config, saadc_callback);
    APP_ERROR_CHECK(err_code);    

    err_code = nrf_drv_saadc_buffer_convert(m_buffer_pool[0], 5);
    APP_ERROR_CHECK(err_code);

    err_code = nrf_drv_saadc_buffer_convert(m_buffer_pool[1], 5);
    APP_ERROR_CHECK(err_code);

    nrf_saadc_int_enable_check(0x0);

}

/**@brief Function for handling the Application's BLE Stack events.
 *
 * @param[in] p_ble_evt  Bluetooth stack event.
 */
static void on_ble_evt(ble_evt_t * p_ble_evt)
{
    uint32_t err_code;

    switch (p_ble_evt->header.evt_id)
    {
        case BLE_GAP_EVT_CONNECTED:

            #if (DEBUG == 1)
                NRF_LOG_INFO("Connected\r\n");
            #endif
           
            nrf_drv_timer_enable(&m_timer);
            saadc_init();
            //configuring gpio pin, happens when device is first connected.
            nrf_gpio_cfg_output(HEAT_EN);
            nrf_gpio_cfg(HEAT_EN,
            NRF_GPIO_PIN_DIR_OUTPUT,
            NRF_GPIO_PIN_INPUT_DISCONNECT,
            NRF_GPIO_PIN_NOPULL,
            NRF_GPIO_PIN_S0H1, // Require High Drive high level: Standard 0, High 1
            NRF_GPIO_PIN_NOSENSE);
          
            //nrf_drv_gpiote_out_set(HEAT_EN);            
            //nrf_drv_gpiote_out_set(NFC_EN);             
       
            err_code = nrf_drv_ppi_channel_enable(m_ppi_channel);
            APP_ERROR_CHECK(err_code);

            m_conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
            break; // BLE_GAP_EVT_CONNECTED

        case BLE_GAP_EVT_DISCONNECTED:
        #if (DEBUG == 1)
            NRF_LOG_INFO("Disconnected, reason %d\r\n",
                          p_ble_evt->evt.gap_evt.params.disconnected.reason);
        #endif
            
            m_conn_handle = BLE_CONN_HANDLE_INVALID;

            nrf_drv_saadc_uninit();
            nrf_drv_timer_disable(&m_timer);
            NVIC_ClearPendingIRQ(SAADC_IRQn);

            //clear gpio when device disconnected
            LEDS_OFF(LEDBUTTON_LED_PIN_NO);
            nrf_drv_gpiote_out_clear(HEAT_EN); 
            nrf_drv_gpiote_out_clear(NFC_EN);
                        
            advertising_start();

            break; // BLE_GAP_EVT_DISCONNECTED

        case BLE_GATTC_EVT_TIMEOUT:
            // Disconnect on GATT Client timeout event.
            #if (DEBUG == 1)
                NRF_LOG_DEBUG("GATT Client Timeout.\r\n");
            #endif

            err_code = sd_ble_gap_disconnect(p_ble_evt->evt.gattc_evt.conn_handle,
                                             BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION);
            APP_ERROR_CHECK(err_code);
            break; // BLE_GATTC_EVT_TIMEOUT

        case BLE_GATTS_EVT_TIMEOUT:
            // Disconnect on GATT Server timeout event.
            #if (DEBUG == 1)
                NRF_LOG_DEBUG("GATT Server Timeout.\r\n");
            #endif
            err_code = sd_ble_gap_disconnect(p_ble_evt->evt.gatts_evt.conn_handle,
                                             BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION);
            APP_ERROR_CHECK(err_code);
            break; // BLE_GATTS_EVT_TIMEOUT

        case BLE_EVT_USER_MEM_REQUEST:
            err_code = sd_ble_user_mem_reply(m_conn_handle, NULL);
            APP_ERROR_CHECK(err_code);
            break; // BLE_EVT_USER_MEM_REQUEST

        case BLE_GATTS_EVT_RW_AUTHORIZE_REQUEST:
        {
            ble_gatts_evt_rw_authorize_request_t  req;
            ble_gatts_rw_authorize_reply_params_t auth_reply;

            req = p_ble_evt->evt.gatts_evt.params.authorize_request;

            if (req.type != BLE_GATTS_AUTHORIZE_TYPE_INVALID)
            {
                if ((req.request.write.op == BLE_GATTS_OP_PREP_WRITE_REQ)     ||
                    (req.request.write.op == BLE_GATTS_OP_EXEC_WRITE_REQ_NOW) ||
                    (req.request.write.op == BLE_GATTS_OP_EXEC_WRITE_REQ_CANCEL))
                {
                    if (req.type == BLE_GATTS_AUTHORIZE_TYPE_WRITE)
                    {
                        auth_reply.type = BLE_GATTS_AUTHORIZE_TYPE_WRITE;
                    }
                    else
                    {
                        auth_reply.type = BLE_GATTS_AUTHORIZE_TYPE_READ;
                    }
                    auth_reply.params.write.gatt_status = APP_FEATURE_NOT_SUPPORTED;
                    err_code = sd_ble_gatts_rw_authorize_reply(p_ble_evt->evt.gatts_evt.conn_handle,
                                                               &auth_reply);
                    APP_ERROR_CHECK(err_code);
                }
            }
        } break; // BLE_GATTS_EVT_RW_AUTHORIZE_REQUEST


        default:
            // No implementation needed.
            break;
    }
}


/**@brief Function for dispatching a BLE stack event to all modules with a BLE stack event handler.
 *
 * @details This function is called from the BLE Stack event interrupt handler after a BLE stack
 *          event has been received.
 *
 * @param[in] p_ble_evt  Bluetooth stack event.
 */
static void ble_evt_dispatch(ble_evt_t * p_ble_evt)
{
    ble_conn_state_on_ble_evt(p_ble_evt);
    pm_on_ble_evt(p_ble_evt);
    ble_hrs_on_ble_evt(&m_hrs, p_ble_evt);
    ble_conn_params_on_ble_evt(p_ble_evt);
    bsp_btn_ble_on_ble_evt(p_ble_evt);
    on_ble_evt(p_ble_evt);
    ble_advertising_on_ble_evt(p_ble_evt);
    nrf_ble_gatt_on_ble_evt(&m_gatt, p_ble_evt);
    ble_lbs_on_ble_evt(&m_lbs, p_ble_evt);
}


/**@brief Function for dispatching a system event to interested modules.
 *
 * @details This function is called from the System event interrupt handler after a system
 *          event has been received.
 *
 * @param[in] sys_evt  System stack event.
 */
static void sys_evt_dispatch(uint32_t sys_evt)
{
    // Dispatch the system event to the fstorage module, where it will be
    // dispatched to the Flash Data Storage (FDS) module.
    fs_sys_event_handler(sys_evt);

    // Dispatch to the Advertising module last, since it will check if there are any
    // pending flash operations in fstorage. Let fstorage process system events first,
    // so that it can report correctly to the Advertising module.
    ble_advertising_on_sys_evt(sys_evt);
}


/**@brief Function for initializing the BLE stack.
 *
 * @details Initializes the SoftDevice and the BLE event interrupt.
 */
static void ble_stack_init(void)
{
    uint32_t err_code;

    nrf_clock_lf_cfg_t clock_lf_cfg = NRF_CLOCK_LFCLKSRC;

    // Initialize the SoftDevice handler module.
    SOFTDEVICE_HANDLER_INIT(&clock_lf_cfg, NULL);

    ble_enable_params_t ble_enable_params;
    err_code = softdevice_enable_get_default_config(NRF_BLE_CENTRAL_LINK_COUNT,
                                                    NRF_BLE_PERIPHERAL_LINK_COUNT,
                                                    &ble_enable_params);
    APP_ERROR_CHECK(err_code);

    // Check the ram settings against the used number of links
    CHECK_RAM_START_ADDR(NRF_BLE_CENTRAL_LINK_COUNT, NRF_BLE_PERIPHERAL_LINK_COUNT);

    // Enable BLE stack.
#if (NRF_SD_BLE_API_VERSION == 3)
    ble_enable_params.gatt_enable_params.att_mtu = NRF_BLE_GATT_MAX_MTU_SIZE;
#endif
    err_code = softdevice_enable(&ble_enable_params);
    APP_ERROR_CHECK(err_code);

    // Register with the SoftDevice handler module for BLE events.
    err_code = softdevice_ble_evt_handler_set(ble_evt_dispatch);
    APP_ERROR_CHECK(err_code);

    // Register with the SoftDevice handler module for BLE events.
    err_code = softdevice_sys_evt_handler_set(sys_evt_dispatch);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling events from the BSP module.
 *
 * @param[in]   event   Event generated by button press.
 */
void bsp_event_handler(bsp_event_t event)
{
    uint32_t err_code;

    switch (event)
    {
        case BSP_EVENT_SLEEP:
            sleep_mode_enter();
            break;

        case BSP_EVENT_DISCONNECT:
            err_code = sd_ble_gap_disconnect(m_conn_handle,
                                             BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION);
            if (err_code != NRF_ERROR_INVALID_STATE)
            {
                APP_ERROR_CHECK(err_code);
            }
            break;

        case BSP_EVENT_WHITELIST_OFF:
            if (m_conn_handle == BLE_CONN_HANDLE_INVALID)
            {
                err_code = ble_advertising_restart_without_whitelist();
                if (err_code != NRF_ERROR_INVALID_STATE)
                {
                    APP_ERROR_CHECK(err_code);
                }
            }
            break;

       

        default:
            break;
    }
}




/**@brief Function for initializing the Advertising functionality.
 */
static void advertising_init(void)
{
    uint32_t               err_code;
    ble_advdata_t          advdata;
    ble_adv_modes_config_t options;

    // Build advertising data struct to pass into @ref ble_advertising_init.
    memset(&advdata, 0, sizeof(advdata));

    advdata.name_type               = BLE_ADVDATA_FULL_NAME;
    advdata.include_appearance      = true;
    advdata.flags                   = BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE;
    advdata.uuids_complete.uuid_cnt = sizeof(m_adv_uuids) / sizeof(m_adv_uuids[0]);
    advdata.uuids_complete.p_uuids  = m_adv_uuids;

    memset(&options, 0, sizeof(options));
    options.ble_adv_fast_enabled  = true;
    options.ble_adv_fast_interval = APP_ADV_INTERVAL;
    options.ble_adv_fast_timeout  = APP_ADV_TIMEOUT_IN_SECONDS;

    err_code = ble_advertising_init(&advdata, NULL, &options, on_adv_evt, NULL);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for initializing buttons and leds.
 *
 * @param[out] p_erase_bonds  Will be true if the clear bonding button was pressed to wake the application up.
 */
static void buttons_leds_init(bool * p_erase_bonds)
{
    bsp_event_t startup_event;

    uint32_t err_code = bsp_init(BSP_INIT_LED | BSP_INIT_BUTTONS,
                                 APP_TIMER_TICKS(100, APP_TIMER_PRESCALER),
                                 bsp_event_handler);

    APP_ERROR_CHECK(err_code);

    err_code = bsp_btn_ble_init(NULL, &startup_event);
    APP_ERROR_CHECK(err_code);

    *p_erase_bonds = (startup_event == BSP_EVENT_CLEAR_BONDING_DATA);
}


/**@brief Function for the Power manager.
 */
static void power_manage(void)
{
    uint32_t err_code = sd_app_evt_wait();

    APP_ERROR_CHECK(err_code);
}

/*GATT generic Event handler*/
void gatt_evt_handler(nrf_ble_gatt_t * p_gatt, nrf_ble_gatt_evt_t * p_evt)
{
    ble_hrs_on_gatt_evt(&m_hrs, p_evt);
}


/*GATT Module init*/
void gatt_init(void)
{
    ret_code_t err_code = nrf_ble_gatt_init(&m_gatt, gatt_evt_handler);
    APP_ERROR_CHECK(err_code);
}

void timer_handler(nrf_timer_event_t event_type, void * p_context)
{

}


void saadc_sampling_event_init(void)
{
    ret_code_t err_code;

    err_code = nrf_drv_ppi_init();
    APP_ERROR_CHECK(err_code);

    nrf_drv_timer_config_t timer_cfg = NRF_DRV_TIMER_DEFAULT_CONFIG;
    timer_cfg.bit_width = NRF_TIMER_BIT_WIDTH_32;
    err_code = nrf_drv_timer_init(&m_timer, &timer_cfg, timer_handler);
    APP_ERROR_CHECK(err_code);

    /* setup m_timer for compare event every 400ms */
    uint32_t ticks = nrf_drv_timer_ms_to_ticks(&m_timer, 2); 
    nrf_drv_timer_extended_compare(&m_timer,
                                   NRF_TIMER_CC_CHANNEL0,
                                   ticks,
                                   NRF_TIMER_SHORT_COMPARE0_CLEAR_MASK,
                                   false);

    uint32_t timer_compare_event_addr = nrf_drv_timer_compare_event_address_get(&m_timer,
                                                                                NRF_TIMER_CC_CHANNEL0);
    uint32_t saadc_sample_task_addr   = nrf_drv_saadc_sample_task_get();

    /* setup ppi channel so that timer compare event is triggering sample task in SAADC */
    err_code = nrf_drv_ppi_channel_alloc(&m_ppi_channel);
    APP_ERROR_CHECK(err_code);

    err_code = nrf_drv_ppi_channel_assign(m_ppi_channel,
                                          timer_compare_event_addr,
                                          saadc_sample_task_addr);
    APP_ERROR_CHECK(err_code);
}


void saadc_sampling_event_enable(void)
{
    ret_code_t err_code = nrf_drv_ppi_channel_enable(m_ppi_channel);

    APP_ERROR_CHECK(err_code);
}



static void gpio_init(void)
{
    ret_code_t  err_code; 

    if(!nrf_drv_gpiote_is_init())
    {
        err_code = nrf_drv_gpiote_init();
    }
    APP_ERROR_CHECK(err_code);

    nrf_drv_gpiote_out_config_t out_config = GPIOTE_CONFIG_OUT_SIMPLE(false);

    err_code = nrf_drv_gpiote_out_init(HEAT_EN, &out_config);
    APP_ERROR_CHECK(err_code);
    err_code = nrf_drv_gpiote_out_init(NFC_EN, &out_config);
    APP_ERROR_CHECK(err_code);
   
}

/**@brief Function for application main entry.
 */
int main(void)
{
    uint32_t err_code;
    bool     erase_bonds;
    
    NRF_POWER->DCDCEN = 1;   // Initialize.
    
    #if (DEBUG == 1)
    err_code = NRF_LOG_INIT(NULL);
    APP_ERROR_CHECK(err_code);
    NRF_LOG_INFO("Heart Rate Sensor Start!\r\n");
    #endif
    int i=0;

    for(i=0;i<5;i++)
    {
        temp_avg[i] = 0;
    }

    gpio_init();
    timers_init();
    buttons_leds_init(&erase_bonds);
    ble_stack_init();

    if (erase_bonds == true)
    {
        #if (DEBUG == 1)
         NRF_LOG_INFO("Bonds erased!\r\n");
        #endif
    }
    gap_params_init();
    advertising_init();
    gatt_init();
    services_init();
    conn_params_init();
    saadc_sampling_event_init();
    
    nrf_gpio_cfg(NFC_EN,
            NRF_GPIO_PIN_DIR_OUTPUT,
            NRF_GPIO_PIN_INPUT_DISCONNECT,
            NRF_GPIO_PIN_NOPULL,
            NRF_GPIO_PIN_H0S1, 
            NRF_GPIO_PIN_NOSENSE);
    nrf_gpio_cfg_output(NFC_EN);
    nrf_drv_gpiote_out_set(NFC_EN);  

    advertising_start();

    // Enter main loop.
    for (;;)
    {
            power_manage();        
    }
}


