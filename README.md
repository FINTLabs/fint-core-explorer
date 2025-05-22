# fint-core-explorer

**Exports custom metrics for FINT core components.**

`fint-core-explorer`—also known as the **Health Check Service**—performs periodic health checks on each configured FINT component and organization. It accesses each component’s `/admin/health` endpoint and emits a metric:

- **1** if the adapter is available
- **0** if the adapter is unavailable

These metrics can be visualized in Grafana for real-time monitoring of your FINT ecosystem.